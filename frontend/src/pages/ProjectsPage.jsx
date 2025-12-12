import { useState, useEffect } from "react";
import { useNavigate } from "react-router-dom";
import { projectAPI, sessionAPI } from "../services/api";
import "./ProjectsPage.css";

const ProjectsPage = () => {
  const navigate = useNavigate();
  const [projects, setProjects] = useState([]);
  const [loading, setLoading] = useState(true);
  const [error, setError] = useState(null);
  const [showModal, setShowModal] = useState(false);
  const [showSessionsModal, setShowSessionsModal] = useState(false);
  const [editingProject, setEditingProject] = useState(null);
  const [selectedProject, setSelectedProject] = useState(null);
  const [projectSessions, setProjectSessions] = useState([]);
  const [loadingSessions, setLoadingSessions] = useState(false);
  const [filterArchived, setFilterArchived] = useState(false);
  const [searchQuery, setSearchQuery] = useState("");
  const [formData, setFormData] = useState({
    name: "",
    description: "",
    color: "#3B82F6",
    icon: "folder",
  });

  const availableIcons = [
    "folder",
    "briefcase",
    "star",
    "heart",
    "bookmark",
    "tag",
    "code",
    "database",
  ];
  const availableColors = [
    "#3B82F6",
    "#10B981",
    "#F59E0B",
    "#EF4444",
    "#8B5CF6",
    "#EC4899",
    "#14B8A6",
    "#F97316",
  ];

  useEffect(() => {
    fetchProjects();
  }, [filterArchived]);

  const fetchProjects = async () => {
    try {
      setLoading(true);
      setError(null);
      const response = await projectAPI.getProjects(
        0,
        50,
        filterArchived ? true : null
      );
      const data = response.data.data || response.data;
      setProjects(data.projects || []);
    } catch (err) {
      console.error("Error fetching projects:", err);
      setError("Failed to load projects");
    } finally {
      setLoading(false);
    }
  };

  const handleSearch = async () => {
    if (!searchQuery.trim()) {
      fetchProjects();
      return;
    }
    try {
      setLoading(true);
      const response = await projectAPI.searchProjects(searchQuery);
      const data = response.data.data || response.data;
      setProjects(data.projects || []);
    } catch (err) {
      console.error("Error searching projects:", err);
      setError("Search failed");
    } finally {
      setLoading(false);
    }
  };

  const openCreateModal = () => {
    setEditingProject(null);
    setFormData({
      name: "",
      description: "",
      color: "#3B82F6",
      icon: "folder",
    });
    setShowModal(true);
  };

  const openEditModal = (project) => {
    setEditingProject(project);
    setFormData({
      name: project.name,
      description: project.description || "",
      color: project.color || "#3B82F6",
      icon: project.icon || "folder",
    });
    setShowModal(true);
  };

  const handleSubmit = async (e) => {
    e.preventDefault();
    try {
      if (editingProject) {
        await projectAPI.updateProject(editingProject.id, formData);
      } else {
        await projectAPI.createProject(formData);
      }
      setShowModal(false);
      fetchProjects();
    } catch (err) {
      console.error("Error saving project:", err);
      alert(err.response?.data?.message || "Failed to save project");
    }
  };

  const handleArchive = async (projectId, isArchived) => {
    try {
      if (isArchived) {
        await projectAPI.unarchiveProject(projectId);
      } else {
        await projectAPI.archiveProject(projectId);
      }
      fetchProjects();
    } catch (err) {
      console.error("Error archiving project:", err);
      alert("Failed to archive/unarchive project");
    }
  };

  const handleDelete = async (projectId) => {
    // Geçici olarak devre dışı - backend'de 500 hatası var
    alert(
      "Project deletion is temporarily disabled due to a backend issue. Please try archiving the project instead."
    );
    return;

    if (
      !confirm(
        "Are you sure you want to delete this project? Sessions will not be deleted, only removed from this project."
      )
    ) {
      return;
    }
    try {
      // Backend zaten session'ları projeden kaldırıyor, sadece projeyi sil
      await projectAPI.deleteProject(projectId);

      // localStorage'dan bu projeye ait session mapping'lerini temizle
      const mapping = JSON.parse(
        localStorage.getItem("sessionProjectMapping") || "{}"
      );
      Object.keys(mapping).forEach((sessionId) => {
        if (mapping[sessionId] === projectId) {
          delete mapping[sessionId];
        }
      });
      localStorage.setItem("sessionProjectMapping", JSON.stringify(mapping));

      fetchProjects();
    } catch (err) {
      console.error("Error deleting project:", err);
      console.error("Full error details:", err.response);
      const errorMsg =
        err.response?.data?.message ||
        err.response?.data?.error ||
        "Failed to delete project. There may be a backend error. Check backend logs.";
      alert(errorMsg);
    }
  };

  const handleViewSessions = async (project) => {
    setSelectedProject(project);
    setShowSessionsModal(true);
    setLoadingSessions(true);

    try {
      // Tüm session'ları çek
      const response = await sessionAPI.getSessions();
      const data = response.data.data || response.data;
      const allSessions = data.sessions || data || [];

      // localStorage'dan session-project mapping'ini al
      const mapping = JSON.parse(
        localStorage.getItem("sessionProjectMapping") || "{}"
      );

      // Bu projeye ait session'ları filtrele
      const filteredSessions = allSessions.filter(
        (session) => mapping[session.sessionId] === project.id
      );

      setProjectSessions(filteredSessions);
    } catch (err) {
      console.error("Error fetching project sessions:", err);
      setProjectSessions([]);
    } finally {
      setLoadingSessions(false);
    }
  };
  const handleSessionClick = (sessionId) => {
    navigate(`/chat?session=${sessionId}`);
  };

  const getIconEmoji = (icon) => {
    const icons = {
      folder: "📁",
      briefcase: "💼",
      star: "⭐",
      heart: "❤️",
      bookmark: "🔖",
      tag: "🏷️",
      code: "💻",
      database: "🗄️",
    };
    return icons[icon] || "📁";
  };

  if (loading && projects.length === 0) {
    return (
      <div className="projects-page">
        <div className="loading-state">Loading projects...</div>
      </div>
    );
  }

  return (
    <div className="projects-page">
      <div className="projects-header">
        <div className="header-top">
          <h1>📁 My Projects</h1>
          <button className="btn-primary" onClick={openCreateModal}>
            + New Project
          </button>
        </div>

        <div className="projects-filters">
          <div className="search-box">
            <input
              type="text"
              placeholder="Search projects..."
              value={searchQuery}
              onChange={(e) => setSearchQuery(e.target.value)}
              onKeyPress={(e) => e.key === "Enter" && handleSearch()}
            />
            <button onClick={handleSearch}>🔍</button>
          </div>

          <label className="filter-checkbox">
            <input
              type="checkbox"
              checked={filterArchived}
              onChange={(e) => setFilterArchived(e.target.checked)}
            />
            Show archived only
          </label>
        </div>
      </div>

      {error && <div className="error-message">{error}</div>}

      <div className="projects-grid">
        {projects.length === 0 ? (
          <div className="empty-state">
            <p>
              No projects found. Create your first project to organize your chat
              sessions!
            </p>
          </div>
        ) : (
          projects.map((project) => (
            <div
              key={project.id}
              className="project-card"
              style={{ borderLeftColor: project.color }}
            >
              <div className="project-header">
                <div
                  className="project-icon"
                  style={{ backgroundColor: project.color }}
                >
                  {getIconEmoji(project.icon)}
                </div>
                <div className="project-info">
                  <h3>{project.name}</h3>
                  <p className="project-description">
                    {project.description || "No description"}
                  </p>
                </div>
                {project.isArchived && (
                  <span className="badge badge-archived">Archived</span>
                )}
              </div>

              <div className="project-stats">
                <span>📊 {project.sessionCount || 0} sessions</span>
                <span>
                  📅 {new Date(project.createdAt).toLocaleDateString()}
                </span>
              </div>

              <div className="project-actions">
                <button
                  className="btn-icon btn-view"
                  onClick={() => handleViewSessions(project)}
                  title="View Sessions"
                >
                  👁️
                </button>
                <button
                  className="btn-icon"
                  onClick={() => openEditModal(project)}
                  title="Edit"
                >
                  ✏️
                </button>
                <button
                  className="btn-icon"
                  onClick={() => handleArchive(project.id, project.isArchived)}
                  title={project.isArchived ? "Unarchive" : "Archive"}
                >
                  {project.isArchived ? "📥" : "📦"}
                </button>
              </div>
            </div>
          ))
        )}
      </div>

      {showModal && (
        <div className="modal-overlay" onClick={() => setShowModal(false)}>
          <div className="modal-content" onClick={(e) => e.stopPropagation()}>
            <div className="modal-header">
              <h2>{editingProject ? "Edit Project" : "Create New Project"}</h2>
              <button className="btn-close" onClick={() => setShowModal(false)}>
                ✕
              </button>
            </div>

            <form onSubmit={handleSubmit}>
              <div className="form-group">
                <label>Project Name *</label>
                <input
                  type="text"
                  value={formData.name}
                  onChange={(e) =>
                    setFormData({ ...formData, name: e.target.value })
                  }
                  required
                  maxLength={100}
                  placeholder="e.g., Work Projects"
                />
              </div>

              <div className="form-group">
                <label>Description</label>
                <textarea
                  value={formData.description}
                  onChange={(e) =>
                    setFormData({ ...formData, description: e.target.value })
                  }
                  maxLength={500}
                  rows={3}
                  placeholder="Optional description..."
                />
              </div>

              <div className="form-group">
                <label>Icon</label>
                <div className="icon-selector">
                  {availableIcons.map((icon) => (
                    <button
                      key={icon}
                      type="button"
                      className={`icon-option ${
                        formData.icon === icon ? "selected" : ""
                      }`}
                      onClick={() => setFormData({ ...formData, icon })}
                    >
                      {getIconEmoji(icon)}
                    </button>
                  ))}
                </div>
              </div>

              <div className="form-group">
                <label>Color</label>
                <div className="color-selector">
                  {availableColors.map((color) => (
                    <button
                      key={color}
                      type="button"
                      className={`color-option ${
                        formData.color === color ? "selected" : ""
                      }`}
                      style={{ backgroundColor: color }}
                      onClick={() => setFormData({ ...formData, color })}
                    />
                  ))}
                </div>
              </div>

              <div className="modal-footer">
                <button
                  type="button"
                  className="btn-secondary"
                  onClick={() => setShowModal(false)}
                >
                  Cancel
                </button>
                <button type="submit" className="btn-primary">
                  {editingProject ? "Save Changes" : "Create Project"}
                </button>
              </div>
            </form>
          </div>
        </div>
      )}

      {/* Sessions Modal */}
      {showSessionsModal && (
        <div
          className="modal-overlay"
          onClick={() => setShowSessionsModal(false)}
        >
          <div
            className="modal-content sessions-modal"
            onClick={(e) => e.stopPropagation()}
          >
            <div className="modal-header">
              <h2>
                {getIconEmoji(selectedProject?.icon)} {selectedProject?.name} -
                Sessions
              </h2>
              <button
                className="btn-close"
                onClick={() => setShowSessionsModal(false)}
              >
                ✕
              </button>
            </div>

            <div className="sessions-modal-content">
              {loadingSessions ? (
                <div className="loading-state">Loading sessions...</div>
              ) : projectSessions.length === 0 ? (
                <div className="empty-state">
                  <p>No sessions in this project yet.</p>
                  <p
                    style={{
                      fontSize: "0.9rem",
                      marginTop: "0.5rem",
                      color: "rgba(255,255,255,0.6)",
                    }}
                  >
                    Add sessions to this project from the chat page using the
                    session menu.
                  </p>
                </div>
              ) : (
                <div className="sessions-list">
                  {projectSessions.map((session) => (
                    <div
                      key={session.sessionId}
                      className="session-item-card"
                      onClick={() => handleSessionClick(session.sessionId)}
                    >
                      <div className="session-item-header">
                        <h4>{session.title || "Untitled Session"}</h4>
                        <span
                          className={`status-badge status-${session.status?.toLowerCase()}`}
                        >
                          {session.status}
                        </span>
                      </div>
                      <div className="session-item-meta">
                        <span>💬 {session.messageCount || 0} messages</span>
                        <span>
                          📅 {new Date(session.createdAt).toLocaleDateString()}
                        </span>
                      </div>
                    </div>
                  ))}
                </div>
              )}
            </div>

            <div className="modal-footer">
              <button
                type="button"
                className="btn-secondary"
                onClick={() => setShowSessionsModal(false)}
              >
                Close
              </button>
            </div>
          </div>
        </div>
      )}
    </div>
  );
};

export default ProjectsPage;
