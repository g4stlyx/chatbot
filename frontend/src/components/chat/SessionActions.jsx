import React, { useState, useRef, useEffect } from "react";
import { sessionAPI, projectAPI } from "../../services/api";
import "./SessionActions.css";

const SessionActions = ({
  session,
  onRename,
  onArchive,
  onPause,
  onActivate,
  onDelete,
  onVisibilityToggle,
  onProjectAssign,
}) => {
  const [showMenu, setShowMenu] = useState(false);
  const [showProjectMenu, setShowProjectMenu] = useState(false);
  const [projects, setProjects] = useState([]);
  const [loadingProjects, setLoadingProjects] = useState(false);
  const menuRef = useRef(null);

  const isActive = session.status === "ACTIVE";
  const isPaused = session.status === "PAUSED";
  const isArchived = session.status === "ARCHIVED";
  const isPublic = session.isPublic;

  // Close menu when clicking outside
  useEffect(() => {
    const handleClickOutside = (event) => {
      if (menuRef.current && !menuRef.current.contains(event.target)) {
        setShowMenu(false);
        setShowProjectMenu(false);
      }
    };

    if (showMenu || showProjectMenu) {
      document.addEventListener("mousedown", handleClickOutside);
    }

    return () => {
      document.removeEventListener("mousedown", handleClickOutside);
    };
  }, [showMenu, showProjectMenu]);

  const loadProjects = async () => {
    if (projects.length > 0) return;

    try {
      setLoadingProjects(true);
      const response = await projectAPI.getProjects(0, 50, false);
      const data = response.data.data || response.data;
      setProjects(data.projects || []);
    } catch (error) {
      console.error("Failed to load projects:", error);
    } finally {
      setLoadingProjects(false);
    }
  };

  const handleAction = (action, callback) => {
    setShowMenu(false);
    setShowProjectMenu(false);
    if (callback) {
      callback(session.sessionId);
    }
  };

  const handleToggleVisibility = async () => {
    try {
      // Mevcut durumun tersini gönder (toggle)
      const newVisibility = !session.isPublic;
      await sessionAPI.toggleVisibility(session.sessionId, newVisibility);
      setShowMenu(false);
      if (onVisibilityToggle) {
        onVisibilityToggle(session.sessionId);
      }
      // Refresh the page or update state
      window.location.reload();
    } catch (error) {
      console.error("Failed to toggle visibility:", error);
      alert("Failed to change session visibility");
    }
  };

  const handleProjectAssign = async (projectId) => {
    try {
      await projectAPI.addSession(projectId, session.sessionId);

      // localStorage'da session-project mapping'i güncelle
      const mapping = JSON.parse(
        localStorage.getItem("sessionProjectMapping") || "{}"
      );
      mapping[session.sessionId] = projectId;
      localStorage.setItem("sessionProjectMapping", JSON.stringify(mapping));

      setShowProjectMenu(false);
      setShowMenu(false);
      if (onProjectAssign) {
        onProjectAssign(session.sessionId, projectId);
      }
      alert("Session added to project successfully!");
    } catch (error) {
      console.error("Failed to add session to project:", error);
      alert(
        error.response?.data?.message || "Failed to add session to project"
      );
    }
  };

  return (
    <div className="session-actions" ref={menuRef}>
      <button
        className="session-actions-trigger"
        onClick={(e) => {
          e.stopPropagation();
          setShowMenu(!showMenu);
        }}
        aria-label="Session actions"
        title="More options"
      >
        <svg
          width="16"
          height="16"
          viewBox="0 0 24 24"
          fill="none"
          stroke="currentColor"
          strokeWidth="2"
        >
          <circle cx="12" cy="12" r="1" />
          <circle cx="12" cy="5" r="1" />
          <circle cx="12" cy="19" r="1" />
        </svg>
      </button>

      {showMenu && (
        <div className="session-actions-menu">
          <button
            className="menu-item"
            onClick={() => handleAction("rename", onRename)}
          >
            <svg
              width="16"
              height="16"
              viewBox="0 0 24 24"
              fill="none"
              stroke="currentColor"
              strokeWidth="2"
            >
              <path d="M11 4H4a2 2 0 0 0-2 2v14a2 2 0 0 0 2 2h14a2 2 0 0 0 2-2v-7" />
              <path d="M18.5 2.5a2.121 2.121 0 0 1 3 3L12 15l-4 1 1-4 9.5-9.5z" />
            </svg>
            <span>Rename</span>
          </button>

          <button
            className="menu-item"
            onClick={(e) => {
              e.stopPropagation();
              setShowProjectMenu(!showProjectMenu);
              if (!showProjectMenu) loadProjects();
            }}
          >
            <svg
              width="16"
              height="16"
              viewBox="0 0 24 24"
              fill="none"
              stroke="currentColor"
              strokeWidth="2"
            >
              <path d="M22 19a2 2 0 0 1-2 2H4a2 2 0 0 1-2-2V5a2 2 0 0 1 2-2h5l2 3h9a2 2 0 0 1 2 2z" />
            </svg>
            <span>Add to Project {showProjectMenu ? "▲" : "▼"}</span>
          </button>

          {showProjectMenu && (
            <div className="submenu">
              {loadingProjects ? (
                <div className="submenu-loading">Loading...</div>
              ) : projects.length === 0 ? (
                <div className="submenu-empty">No projects yet</div>
              ) : (
                projects.map((project) => (
                  <button
                    key={project.id}
                    className="submenu-item"
                    onClick={() => handleProjectAssign(project.id)}
                  >
                    <span style={{ marginRight: "8px" }}>
                      {project.icon === "folder"
                        ? "📁"
                        : project.icon === "briefcase"
                        ? "💼"
                        : project.icon === "star"
                        ? "⭐"
                        : "📁"}
                    </span>
                    {project.name}
                  </button>
                ))
              )}
            </div>
          )}

          <button className="menu-item" onClick={handleToggleVisibility}>
            <svg
              width="16"
              height="16"
              viewBox="0 0 24 24"
              fill="none"
              stroke="currentColor"
              strokeWidth="2"
            >
              {isPublic ? (
                <>
                  <path d="M17.94 17.94A10.07 10.07 0 0 1 12 20c-7 0-11-8-11-8a18.45 18.45 0 0 1 5.06-5.94" />
                  <path d="M1 1l22 22" />
                  <path d="M9.9 4.24A9.12 9.12 0 0 1 12 4c7 0 11 8 11 8a18.5 18.5 0 0 1-2.16 3.19" />
                </>
              ) : (
                <>
                  <path d="M1 12s4-8 11-8 11 8 11 8-4 8-11 8-11-8-11-8z" />
                  <circle cx="12" cy="12" r="3" />
                </>
              )}
            </svg>
            <span>{isPublic ? "Make Private" : "Make Public"}</span>
          </button>

          {isActive && (
            <button
              className="menu-item"
              onClick={() => handleAction("pause", onPause)}
            >
              <svg
                width="16"
                height="16"
                viewBox="0 0 24 24"
                fill="none"
                stroke="currentColor"
                strokeWidth="2"
              >
                <rect x="6" y="4" width="4" height="16" />
                <rect x="14" y="4" width="4" height="16" />
              </svg>
              <span>Pause</span>
            </button>
          )}

          {isPaused && (
            <button
              className="menu-item"
              onClick={() => handleAction("activate", onActivate)}
            >
              <svg
                width="16"
                height="16"
                viewBox="0 0 24 24"
                fill="none"
                stroke="currentColor"
                strokeWidth="2"
              >
                <polygon points="5 3 19 12 5 21 5 3" />
              </svg>
              <span>Resume</span>
            </button>
          )}

          {!isArchived && (
            <button
              className="menu-item"
              onClick={() => handleAction("archive", onArchive)}
            >
              <svg
                width="16"
                height="16"
                viewBox="0 0 24 24"
                fill="none"
                stroke="currentColor"
                strokeWidth="2"
              >
                <polyline points="21 8 21 21 3 21 3 8" />
                <rect x="1" y="3" width="22" height="5" />
                <line x1="10" y1="12" x2="14" y2="12" />
              </svg>
              <span>Archive</span>
            </button>
          )}

          {isArchived && (
            <button
              className="menu-item"
              onClick={() => handleAction("activate", onActivate)}
            >
              <svg
                width="16"
                height="16"
                viewBox="0 0 24 24"
                fill="none"
                stroke="currentColor"
                strokeWidth="2"
              >
                <polyline points="3 8 3 21 21 21 21 8" />
                <rect x="1" y="3" width="22" height="5" />
                <line x1="10" y1="12" x2="14" y2="12" />
              </svg>
              <span>Unarchive</span>
            </button>
          )}

          <div className="menu-divider"></div>

          <button
            className="menu-item danger"
            onClick={() => handleAction("delete", onDelete)}
          >
            <svg
              width="16"
              height="16"
              viewBox="0 0 24 24"
              fill="none"
              stroke="currentColor"
              strokeWidth="2"
            >
              <polyline points="3 6 5 6 21 6" />
              <path d="M19 6v14a2 2 0 0 1-2 2H7a2 2 0 0 1-2-2V6m3 0V4a2 2 0 0 1 2-2h4a2 2 0 0 1 2 2v2" />
            </svg>
            <span>Delete</span>
          </button>
        </div>
      )}
    </div>
  );
};

export default SessionActions;
