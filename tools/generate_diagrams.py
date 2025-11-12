"""
Chatbot System Diagram Generator
Generates:
1. Database UML diagram showing all entities and relationships
2. System architecture diagram showing how components interact
"""

from graphviz import Digraph
import os
import sys

# Add Graphviz to PATH for Windows
if sys.platform == 'win32':
    graphviz_paths = [
        r"C:\Program Files\Graphviz\bin",
        r"C:\Program Files (x86)\Graphviz\bin",
        os.path.expanduser(r"~\AppData\Local\Programs\Graphviz\bin"),
    ]
    for path in graphviz_paths:
        if os.path.exists(path) and path not in os.environ["PATH"]:
            os.environ["PATH"] += os.pathsep + path


def generate_database_uml():
    """Generate UML diagram for database structure"""
    dot = Digraph(comment='Chatbot Database Schema', format='png')
    dot.attr(rankdir='TB', splines='ortho', nodesep='0.8', ranksep='1.2')
    dot.attr('node', shape='record', style='filled', fillcolor='lightblue', 
             fontname='Arial', fontsize='10')
    dot.attr('edge', fontname='Arial', fontsize='9')

    # User Entity
    dot.node('User', '''<<TABLE BORDER="0" CELLBORDER="1" CELLSPACING="0" CELLPADDING="4">
        <TR><TD BGCOLOR="#4A90E2" COLSPAN="2"><B>User</B></TD></TR>
        <TR><TD ALIGN="LEFT">🔑 id: Long</TD><TD ALIGN="LEFT">PK</TD></TR>
        <TR><TD ALIGN="LEFT">username: String(50)</TD><TD ALIGN="LEFT">UNIQUE, NOT NULL</TD></TR>
        <TR><TD ALIGN="LEFT">email: String(255)</TD><TD ALIGN="LEFT">UNIQUE, NOT NULL</TD></TR>
        <TR><TD ALIGN="LEFT">password_hash: String</TD><TD ALIGN="LEFT">NOT NULL</TD></TR>
        <TR><TD ALIGN="LEFT">salt: String(64)</TD><TD ALIGN="LEFT">NOT NULL</TD></TR>
        <TR><TD ALIGN="LEFT">first_name: String(100)</TD><TD ALIGN="LEFT"></TD></TR>
        <TR><TD ALIGN="LEFT">last_name: String(100)</TD><TD ALIGN="LEFT"></TD></TR>
        <TR><TD ALIGN="LEFT">profile_picture: String(500)</TD><TD ALIGN="LEFT"></TD></TR>
        <TR><TD ALIGN="LEFT">is_active: Boolean</TD><TD ALIGN="LEFT">DEFAULT true</TD></TR>
        <TR><TD ALIGN="LEFT">email_verified: Boolean</TD><TD ALIGN="LEFT">DEFAULT false</TD></TR>
        <TR><TD ALIGN="LEFT">login_attempts: Integer</TD><TD ALIGN="LEFT">DEFAULT 0</TD></TR>
        <TR><TD ALIGN="LEFT">locked_until: DateTime</TD><TD ALIGN="LEFT"></TD></TR>
        <TR><TD ALIGN="LEFT">created_at: DateTime</TD><TD ALIGN="LEFT">NOT NULL</TD></TR>
        <TR><TD ALIGN="LEFT">updated_at: DateTime</TD><TD ALIGN="LEFT"></TD></TR>
        <TR><TD ALIGN="LEFT">last_login_at: DateTime</TD><TD ALIGN="LEFT"></TD></TR>
    </TABLE>>''', shape='plaintext')

    # Admin Entity
    dot.node('Admin', '''<<TABLE BORDER="0" CELLBORDER="1" CELLSPACING="0" CELLPADDING="4">
        <TR><TD BGCOLOR="#E74C3C" COLSPAN="2"><B>Admin</B></TD></TR>
        <TR><TD ALIGN="LEFT">🔑 id: Long</TD><TD ALIGN="LEFT">PK</TD></TR>
        <TR><TD ALIGN="LEFT">username: String(50)</TD><TD ALIGN="LEFT">UNIQUE, NOT NULL</TD></TR>
        <TR><TD ALIGN="LEFT">email: String(255)</TD><TD ALIGN="LEFT">UNIQUE, NOT NULL</TD></TR>
        <TR><TD ALIGN="LEFT">password_hash: String</TD><TD ALIGN="LEFT">NOT NULL</TD></TR>
        <TR><TD ALIGN="LEFT">salt: String(64)</TD><TD ALIGN="LEFT">NOT NULL</TD></TR>
        <TR><TD ALIGN="LEFT">first_name: String(100)</TD><TD ALIGN="LEFT"></TD></TR>
        <TR><TD ALIGN="LEFT">last_name: String(100)</TD><TD ALIGN="LEFT"></TD></TR>
        <TR><TD ALIGN="LEFT">profile_picture: String(500)</TD><TD ALIGN="LEFT"></TD></TR>
        <TR><TD ALIGN="LEFT">level: Integer</TD><TD ALIGN="LEFT">DEFAULT 2</TD></TR>
        <TR><TD ALIGN="LEFT">is_active: Boolean</TD><TD ALIGN="LEFT">DEFAULT true</TD></TR>
        <TR><TD ALIGN="LEFT">login_attempts: Integer</TD><TD ALIGN="LEFT">DEFAULT 0</TD></TR>
        <TR><TD ALIGN="LEFT">locked_until: DateTime</TD><TD ALIGN="LEFT"></TD></TR>
        <TR><TD ALIGN="LEFT">created_by: Long</TD><TD ALIGN="LEFT">FK</TD></TR>
        <TR><TD ALIGN="LEFT">created_at: DateTime</TD><TD ALIGN="LEFT">NOT NULL</TD></TR>
        <TR><TD ALIGN="LEFT">updated_at: DateTime</TD><TD ALIGN="LEFT"></TD></TR>
        <TR><TD ALIGN="LEFT">last_login_at: DateTime</TD><TD ALIGN="LEFT"></TD></TR>
    </TABLE>>''', shape='plaintext')

    # Admin Permissions (separate table for ElementCollection)
    dot.node('AdminPermissions', '''<<TABLE BORDER="0" CELLBORDER="1" CELLSPACING="0" CELLPADDING="4">
        <TR><TD BGCOLOR="#E67E22" COLSPAN="2"><B>Admin_Permissions</B></TD></TR>
        <TR><TD ALIGN="LEFT">🔑 admin_id: Long</TD><TD ALIGN="LEFT">FK</TD></TR>
        <TR><TD ALIGN="LEFT">permission: String</TD><TD ALIGN="LEFT"></TD></TR>
    </TABLE>>''', shape='plaintext')

    # ChatSession Entity
    dot.node('ChatSession', '''<<TABLE BORDER="0" CELLBORDER="1" CELLSPACING="0" CELLPADDING="4">
        <TR><TD BGCOLOR="#27AE60" COLSPAN="2"><B>ChatSession</B></TD></TR>
        <TR><TD ALIGN="LEFT">🔑 session_id: String(36)</TD><TD ALIGN="LEFT">PK (UUID)</TD></TR>
        <TR><TD ALIGN="LEFT">user_id: Long</TD><TD ALIGN="LEFT">FK, NOT NULL</TD></TR>
        <TR><TD ALIGN="LEFT">title: String(255)</TD><TD ALIGN="LEFT"></TD></TR>
        <TR><TD ALIGN="LEFT">model: String(50)</TD><TD ALIGN="LEFT">DEFAULT 'gpt-3.5-turbo'</TD></TR>
        <TR><TD ALIGN="LEFT">status: Enum</TD><TD ALIGN="LEFT">ACTIVE/PAUSED/ARCHIVED/DELETED</TD></TR>
        <TR><TD ALIGN="LEFT">message_count: Integer</TD><TD ALIGN="LEFT">DEFAULT 0</TD></TR>
        <TR><TD ALIGN="LEFT">token_usage: Long</TD><TD ALIGN="LEFT">DEFAULT 0</TD></TR>
        <TR><TD ALIGN="LEFT">is_public: Boolean</TD><TD ALIGN="LEFT">DEFAULT false</TD></TR>
        <TR><TD ALIGN="LEFT">is_flagged: Boolean</TD><TD ALIGN="LEFT">DEFAULT false</TD></TR>
        <TR><TD ALIGN="LEFT">flag_reason: Text</TD><TD ALIGN="LEFT"></TD></TR>
        <TR><TD ALIGN="LEFT">flagged_by: Long</TD><TD ALIGN="LEFT">FK (Admin)</TD></TR>
        <TR><TD ALIGN="LEFT">flagged_at: DateTime</TD><TD ALIGN="LEFT"></TD></TR>
        <TR><TD ALIGN="LEFT">created_at: DateTime</TD><TD ALIGN="LEFT">NOT NULL</TD></TR>
        <TR><TD ALIGN="LEFT">updated_at: DateTime</TD><TD ALIGN="LEFT"></TD></TR>
        <TR><TD ALIGN="LEFT">last_accessed_at: DateTime</TD><TD ALIGN="LEFT"></TD></TR>
        <TR><TD ALIGN="LEFT">expires_at: DateTime</TD><TD ALIGN="LEFT"></TD></TR>
    </TABLE>>''', shape='plaintext')

    # Message Entity
    dot.node('Message', '''<<TABLE BORDER="0" CELLBORDER="1" CELLSPACING="0" CELLPADDING="4">
        <TR><TD BGCOLOR="#9B59B6" COLSPAN="2"><B>Message</B></TD></TR>
        <TR><TD ALIGN="LEFT">🔑 id: Long</TD><TD ALIGN="LEFT">PK</TD></TR>
        <TR><TD ALIGN="LEFT">session_id: String(36)</TD><TD ALIGN="LEFT">FK, NOT NULL</TD></TR>
        <TR><TD ALIGN="LEFT">role: Enum</TD><TD ALIGN="LEFT">USER/ASSISTANT/SYSTEM</TD></TR>
        <TR><TD ALIGN="LEFT">content: Text</TD><TD ALIGN="LEFT">NOT NULL</TD></TR>
        <TR><TD ALIGN="LEFT">token_count: Integer</TD><TD ALIGN="LEFT"></TD></TR>
        <TR><TD ALIGN="LEFT">model: String(50)</TD><TD ALIGN="LEFT"></TD></TR>
        <TR><TD ALIGN="LEFT">metadata: JSON</TD><TD ALIGN="LEFT"></TD></TR>
        <TR><TD ALIGN="LEFT">is_flagged: Boolean</TD><TD ALIGN="LEFT">DEFAULT false</TD></TR>
        <TR><TD ALIGN="LEFT">flag_reason: Text</TD><TD ALIGN="LEFT"></TD></TR>
        <TR><TD ALIGN="LEFT">flagged_by: Long</TD><TD ALIGN="LEFT">FK (Admin)</TD></TR>
        <TR><TD ALIGN="LEFT">flagged_at: DateTime</TD><TD ALIGN="LEFT"></TD></TR>
        <TR><TD ALIGN="LEFT">timestamp: DateTime</TD><TD ALIGN="LEFT">NOT NULL</TD></TR>
    </TABLE>>''', shape='plaintext')

    # MessageFlag Entity
    dot.node('MessageFlag', '''<<TABLE BORDER="0" CELLBORDER="1" CELLSPACING="0" CELLPADDING="4">
        <TR><TD BGCOLOR="#F39C12" COLSPAN="2"><B>MessageFlag</B></TD></TR>
        <TR><TD ALIGN="LEFT">🔑 id: Long</TD><TD ALIGN="LEFT">PK</TD></TR>
        <TR><TD ALIGN="LEFT">message_id: Long</TD><TD ALIGN="LEFT">FK, NOT NULL</TD></TR>
        <TR><TD ALIGN="LEFT">flagged_by: Long</TD><TD ALIGN="LEFT">FK (Admin), NOT NULL</TD></TR>
        <TR><TD ALIGN="LEFT">flag_type: Enum</TD><TD ALIGN="LEFT">INAPPROPRIATE/SPAM/HARMFUL/OTHER</TD></TR>
        <TR><TD ALIGN="LEFT">reason: Text</TD><TD ALIGN="LEFT"></TD></TR>
        <TR><TD ALIGN="LEFT">status: Enum</TD><TD ALIGN="LEFT">PENDING/REVIEWED/RESOLVED/DISMISSED</TD></TR>
        <TR><TD ALIGN="LEFT">reviewed_by: Long</TD><TD ALIGN="LEFT">FK (Admin)</TD></TR>
        <TR><TD ALIGN="LEFT">reviewed_at: DateTime</TD><TD ALIGN="LEFT"></TD></TR>
        <TR><TD ALIGN="LEFT">created_at: DateTime</TD><TD ALIGN="LEFT">NOT NULL</TD></TR>
    </TABLE>>''', shape='plaintext')

    # AdminActivityLog Entity
    dot.node('AdminActivityLog', '''<<TABLE BORDER="0" CELLBORDER="1" CELLSPACING="0" CELLPADDING="4">
        <TR><TD BGCOLOR="#16A085" COLSPAN="2"><B>AdminActivityLog</B></TD></TR>
        <TR><TD ALIGN="LEFT">🔑 id: Long</TD><TD ALIGN="LEFT">PK</TD></TR>
        <TR><TD ALIGN="LEFT">admin_id: Long</TD><TD ALIGN="LEFT">FK, NOT NULL</TD></TR>
        <TR><TD ALIGN="LEFT">action: String(100)</TD><TD ALIGN="LEFT">NOT NULL</TD></TR>
        <TR><TD ALIGN="LEFT">resource_type: String(50)</TD><TD ALIGN="LEFT">NOT NULL</TD></TR>
        <TR><TD ALIGN="LEFT">resource_id: String(100)</TD><TD ALIGN="LEFT"></TD></TR>
        <TR><TD ALIGN="LEFT">details: JSON</TD><TD ALIGN="LEFT"></TD></TR>
        <TR><TD ALIGN="LEFT">ip_address: String(45)</TD><TD ALIGN="LEFT"></TD></TR>
        <TR><TD ALIGN="LEFT">user_agent: Text</TD><TD ALIGN="LEFT"></TD></TR>
        <TR><TD ALIGN="LEFT">created_at: DateTime</TD><TD ALIGN="LEFT">NOT NULL</TD></TR>
    </TABLE>>''', shape='plaintext')

    # VerificationToken Entity
    dot.node('VerificationToken', '''<<TABLE BORDER="0" CELLBORDER="1" CELLSPACING="0" CELLPADDING="4">
        <TR><TD BGCOLOR="#3498DB" COLSPAN="2"><B>VerificationToken</B></TD></TR>
        <TR><TD ALIGN="LEFT">🔑 id: Long</TD><TD ALIGN="LEFT">PK</TD></TR>
        <TR><TD ALIGN="LEFT">token: String</TD><TD ALIGN="LEFT">UNIQUE, NOT NULL</TD></TR>
        <TR><TD ALIGN="LEFT">user_id: Long</TD><TD ALIGN="LEFT">NOT NULL</TD></TR>
        <TR><TD ALIGN="LEFT">user_type: String</TD><TD ALIGN="LEFT">'user' or 'admin'</TD></TR>
        <TR><TD ALIGN="LEFT">expiry_date: DateTime</TD><TD ALIGN="LEFT">NOT NULL (+24h)</TD></TR>
        <TR><TD ALIGN="LEFT">created_date: DateTime</TD><TD ALIGN="LEFT">NOT NULL</TD></TR>
    </TABLE>>''', shape='plaintext')

    # PasswordResetToken Entity
    dot.node('PasswordResetToken', '''<<TABLE BORDER="0" CELLBORDER="1" CELLSPACING="0" CELLPADDING="4">
        <TR><TD BGCOLOR="#E67E22" COLSPAN="2"><B>PasswordResetToken</B></TD></TR>
        <TR><TD ALIGN="LEFT">🔑 id: Long</TD><TD ALIGN="LEFT">PK</TD></TR>
        <TR><TD ALIGN="LEFT">token: String</TD><TD ALIGN="LEFT">UNIQUE, NOT NULL</TD></TR>
        <TR><TD ALIGN="LEFT">user_id: Long</TD><TD ALIGN="LEFT">NOT NULL</TD></TR>
        <TR><TD ALIGN="LEFT">user_type: String</TD><TD ALIGN="LEFT">'user' or 'admin'</TD></TR>
        <TR><TD ALIGN="LEFT">expiry_date: DateTime</TD><TD ALIGN="LEFT">NOT NULL (+15min)</TD></TR>
        <TR><TD ALIGN="LEFT">created_date: DateTime</TD><TD ALIGN="LEFT">NOT NULL</TD></TR>
        <TR><TD ALIGN="LEFT">attempt_count: Integer</TD><TD ALIGN="LEFT">DEFAULT 0</TD></TR>
        <TR><TD ALIGN="LEFT">requesting_ip: String</TD><TD ALIGN="LEFT"></TD></TR>
    </TABLE>>''', shape='plaintext')

    # Relationships
    # User -> ChatSession (1:N)
    dot.edge('User', 'ChatSession', label='1:N', color='#2C3E50', penwidth='2')
    
    # ChatSession -> Message (1:N)
    dot.edge('ChatSession', 'Message', label='1:N', color='#2C3E50', penwidth='2')
    
    # Admin -> AdminPermissions (1:N)
    dot.edge('Admin', 'AdminPermissions', label='1:N', color='#C0392B', penwidth='2')
    
    # Admin -> AdminActivityLog (1:N)
    dot.edge('Admin', 'AdminActivityLog', label='1:N', color='#C0392B', penwidth='2')
    
    # Admin -> ChatSession (flags)
    dot.edge('Admin', 'ChatSession', label='flags', color='#E74C3C', 
             style='dashed', constraint='false')
    
    # Message -> MessageFlag (1:N)
    dot.edge('Message', 'MessageFlag', label='1:N', color='#8E44AD', penwidth='2')
    
    # Admin -> MessageFlag (flagged_by)
    dot.edge('Admin', 'MessageFlag', label='flags', color='#E74C3C', 
             style='dashed', constraint='false')
    
    # Admin -> MessageFlag (reviewed_by)
    dot.edge('Admin', 'MessageFlag', label='reviews', color='#27AE60', 
             style='dashed', constraint='false')

    return dot


def generate_architecture_diagram():
    """Generate enhanced system architecture diagram"""
    dot = Digraph(comment='Chatbot System Architecture', format='png')
    dot.attr(rankdir='LR', splines='spline', nodesep='1.2', ranksep='2.0', 
             bgcolor='#F8F9FA', dpi='300')
    dot.attr('node', fontname='Segoe UI', fontsize='11')
    dot.attr('edge', fontname='Segoe UI', fontsize='9')

    # Frontend Layer
    with dot.subgraph(name='cluster_frontend') as c:
        c.attr(label='Frontend Layer', style='filled,rounded', 
               color='#E3F2FD', fillcolor='#E3F2FD',
               fontsize='14', fontname='Segoe UI Bold', labeljust='l', 
               penwidth='2', margin='20')
        c.node('Browser', '''<<TABLE BORDER="0" CELLBORDER="1" CELLSPACING="0" CELLPADDING="10" BGCOLOR="white">
            <TR><TD BGCOLOR="#61DAFB" COLSPAN="2"><B><FONT POINT-SIZE="13">&#127760; React Frontend</FONT></B></TD></TR>
            <TR><TD ALIGN="LEFT" COLSPAN="2" BGCOLOR="#E3F2FD"><B>Vite + React 18</B></TD></TR>
            <TR><TD ALIGN="LEFT" COLSPAN="2">Port: 5173</TD></TR>
            <TR><TD ALIGN="LEFT" COLSPAN="2" BGCOLOR="#F5F5F5"><B>Pages:</B></TD></TR>
            <TR><TD ALIGN="LEFT">&#8226; ChatPage</TD><TD ALIGN="LEFT">&#8226; LoginPage</TD></TR>
            <TR><TD ALIGN="LEFT">&#8226; RegisterPage</TD><TD ALIGN="LEFT">&#8226; ProfilePage</TD></TR>
            <TR><TD ALIGN="LEFT">&#8226; ForgotPassword</TD><TD ALIGN="LEFT">&#8226; ResetPassword</TD></TR>
            <TR><TD ALIGN="LEFT">&#8226; EmailVerify</TD><TD ALIGN="LEFT">&#8226; AdminDashboard</TD></TR>
            <TR><TD ALIGN="LEFT" COLSPAN="2" BGCOLOR="#F5F5F5"><B>Features:</B></TD></TR>
            <TR><TD ALIGN="LEFT" COLSPAN="2">&#8226; Context API (Auth, Chat)</TD></TR>
            <TR><TD ALIGN="LEFT" COLSPAN="2">&#8226; Custom Hooks</TD></TR>
            <TR><TD ALIGN="LEFT" COLSPAN="2">&#8226; Component Library</TD></TR>
        </TABLE>>''', shape='plaintext')

    # Backend Layer
    with dot.subgraph(name='cluster_backend') as c:
        c.attr(label='Backend Layer', style='filled,rounded', 
               color='#E8F5E9', fillcolor='#E8F5E9',
               fontsize='14', fontname='Segoe UI Bold', labeljust='l',
               penwidth='2', margin='20')
        c.node('SpringBoot', '''<<TABLE BORDER="0" CELLBORDER="1" CELLSPACING="0" CELLPADDING="10" BGCOLOR="white">
            <TR><TD BGCOLOR="#6DB33F" COLSPAN="2"><B><FONT COLOR="white" POINT-SIZE="13">&#9749; Spring Boot REST API</FONT></B></TD></TR>
            <TR><TD ALIGN="LEFT" COLSPAN="2" BGCOLOR="#E8F5E9"><B>Java 17 + Spring Boot 3.4.4</B></TD></TR>
            <TR><TD ALIGN="LEFT" COLSPAN="2">Port: 8080</TD></TR>
            <TR><TD ALIGN="LEFT" COLSPAN="2" BGCOLOR="#F5F5F5"><B>Controllers (12):</B></TD></TR>
            <TR><TD ALIGN="LEFT">&#8226; AuthController</TD><TD ALIGN="LEFT">&#8226; ChatController</TD></TR>
            <TR><TD ALIGN="LEFT">&#8226; ChatSessionController</TD><TD ALIGN="LEFT">&#8226; MessageController</TD></TR>
            <TR><TD ALIGN="LEFT">&#8226; UserProfileController</TD><TD ALIGN="LEFT">&#8226; UserManagementController</TD></TR>
            <TR><TD ALIGN="LEFT">&#8226; AdminManagementController</TD><TD ALIGN="LEFT">&#8226; AdminProfileController</TD></TR>
            <TR><TD ALIGN="LEFT">&#8226; AdminSessionController</TD><TD ALIGN="LEFT">&#8226; AdminMessageController</TD></TR>
            <TR><TD ALIGN="LEFT">&#8226; AdminActivityLogController</TD><TD ALIGN="LEFT">&#8226; AdminTokenManagement</TD></TR>
            <TR><TD ALIGN="LEFT" COLSPAN="2" BGCOLOR="#F5F5F5"><B>Core Services:</B></TD></TR>
            <TR><TD ALIGN="LEFT" COLSPAN="2">&#8226; JWT Authentication &amp; Authorization</TD></TR>
            <TR><TD ALIGN="LEFT" COLSPAN="2">&#8226; Session &amp; Message Management</TD></TR>
            <TR><TD ALIGN="LEFT" COLSPAN="2">&#8226; User &amp; Admin Services</TD></TR>
            <TR><TD ALIGN="LEFT" COLSPAN="2">&#8226; Email Service (SMTP)</TD></TR>
            <TR><TD ALIGN="LEFT" COLSPAN="2">&#8226; Activity Logging &amp; Auditing</TD></TR>
            <TR><TD ALIGN="LEFT" COLSPAN="2">&#8226; Rate Limiting &amp; Security</TD></TR>
        </TABLE>>''', shape='plaintext')

    # Database Layer
    with dot.subgraph(name='cluster_database') as c:
        c.attr(label='Database Layer', style='filled,rounded', 
               color='#FFF3E0', fillcolor='#FFF3E0',
               fontsize='14', fontname='Segoe UI Bold', labeljust='l',
               penwidth='2', margin='20')
        c.node('MySQL', '''<<TABLE BORDER="0" CELLBORDER="1" CELLSPACING="0" CELLPADDING="10" BGCOLOR="white">
            <TR><TD BGCOLOR="#00758F" COLSPAN="2"><FONT COLOR="white"><B><FONT POINT-SIZE="13">&#128044; MySQL Database</FONT></B></FONT></TD></TR>
            <TR><TD ALIGN="LEFT" COLSPAN="2" BGCOLOR="#FFF3E0"><B>Relational DBMS</B></TD></TR>
            <TR><TD ALIGN="LEFT" COLSPAN="2">Port: 3306</TD></TR>
            <TR><TD ALIGN="LEFT" COLSPAN="2" BGCOLOR="#F5F5F5"><B>Tables (8):</B></TD></TR>
            <TR><TD ALIGN="LEFT">&#8226; users</TD><TD ALIGN="LEFT">&#8226; admins</TD></TR>
            <TR><TD ALIGN="LEFT">&#8226; chat_sessions</TD><TD ALIGN="LEFT">&#8226; messages</TD></TR>
            <TR><TD ALIGN="LEFT">&#8226; message_flags</TD><TD ALIGN="LEFT">&#8226; admin_activity_log</TD></TR>
            <TR><TD ALIGN="LEFT">&#8226; verification_tokens</TD><TD ALIGN="LEFT">&#8226; password_reset_tokens</TD></TR>
            <TR><TD ALIGN="LEFT" COLSPAN="2" BGCOLOR="#F5F5F5"><B>Features:</B></TD></TR>
            <TR><TD ALIGN="LEFT" COLSPAN="2">&#8226; JPA/Hibernate ORM</TD></TR>
            <TR><TD ALIGN="LEFT" COLSPAN="2">&#8226; Connection Pooling</TD></TR>
            <TR><TD ALIGN="LEFT" COLSPAN="2">&#8226; Transaction Management</TD></TR>
        </TABLE>>''', shape='plaintext')

    # Cache Layer
    with dot.subgraph(name='cluster_cache') as c:
        c.attr(label='Cache Layer', style='filled,rounded', 
               color='#FFEBEE', fillcolor='#FFEBEE',
               fontsize='14', fontname='Segoe UI Bold', labeljust='l',
               penwidth='2', margin='20')
        c.node('Redis', '''<<TABLE BORDER="0" CELLBORDER="1" CELLSPACING="0" CELLPADDING="10" BGCOLOR="white">
            <TR><TD BGCOLOR="#DC382D" COLSPAN="2"><FONT COLOR="white"><B><FONT POINT-SIZE="13">&#9889; Redis Cache</FONT></B></FONT></TD></TR>
            <TR><TD ALIGN="LEFT" COLSPAN="2" BGCOLOR="#FFEBEE"><B>In-Memory Data Store</B></TD></TR>
            <TR><TD ALIGN="LEFT" COLSPAN="2">Port: 6379</TD></TR>
            <TR><TD ALIGN="LEFT" COLSPAN="2">Docker: chatbot-redis</TD></TR>
            <TR><TD ALIGN="LEFT" COLSPAN="2" BGCOLOR="#F5F5F5"><B>Use Cases:</B></TD></TR>
            <TR><TD ALIGN="LEFT" COLSPAN="2">&#8226; Session Data Caching</TD></TR>
            <TR><TD ALIGN="LEFT" COLSPAN="2">&#8226; Rate Limiting</TD></TR>
            <TR><TD ALIGN="LEFT" COLSPAN="2">&#8226; JWT Token Blacklist</TD></TR>
            <TR><TD ALIGN="LEFT" COLSPAN="2">&#8226; Temporary Token Storage</TD></TR>
            <TR><TD ALIGN="LEFT" COLSPAN="2">&#8226; Response Caching</TD></TR>
        </TABLE>>''', shape='plaintext')

    # AI/LLM Layer
    with dot.subgraph(name='cluster_ai') as c:
        c.attr(label='AI/LLM Layer', style='filled,rounded', 
               color='#F3E5F5', fillcolor='#F3E5F5',
               fontsize='14', fontname='Segoe UI Bold', labeljust='l',
               penwidth='2', margin='20')
        c.node('Ollama', '''<<TABLE BORDER="0" CELLBORDER="1" CELLSPACING="0" CELLPADDING="10" BGCOLOR="white">
            <TR><TD BGCOLOR="#000000" COLSPAN="2"><FONT COLOR="white"><B><FONT POINT-SIZE="13">&#129302; Ollama LLM Server</FONT></B></FONT></TD></TR>
            <TR><TD ALIGN="LEFT" COLSPAN="2" BGCOLOR="#F3E5F5"><B>Local Language Model</B></TD></TR>
            <TR><TD ALIGN="LEFT" COLSPAN="2">Port: 11434</TD></TR>
            <TR><TD ALIGN="LEFT" COLSPAN="2" BGCOLOR="#F5F5F5"><B>Available Models:</B></TD></TR>
            <TR><TD ALIGN="LEFT" COLSPAN="2">&#8226; llama3 (Default)</TD></TR>
            <TR><TD ALIGN="LEFT" COLSPAN="2">&#8226; Other Compatible Models</TD></TR>
            <TR><TD ALIGN="LEFT" COLSPAN="2" BGCOLOR="#F5F5F5"><B>API Endpoints:</B></TD></TR>
            <TR><TD ALIGN="LEFT" COLSPAN="2">&#8226; POST /api/generate</TD></TR>
            <TR><TD ALIGN="LEFT" COLSPAN="2">&#8226; POST /api/chat</TD></TR>
        </TABLE>>''', shape='plaintext')

    # External Services
    with dot.subgraph(name='cluster_external') as c:
        c.attr(label='External Services', style='filled,rounded', 
               color='#E8EAF6', fillcolor='#E8EAF6',
               fontsize='14', fontname='Segoe UI Bold', labeljust='l',
               penwidth='2', margin='20')
        c.node('Email', '''<<TABLE BORDER="0" CELLBORDER="1" CELLSPACING="0" CELLPADDING="10" BGCOLOR="white">
            <TR><TD BGCOLOR="#EA4335" COLSPAN="2"><FONT COLOR="white"><B><FONT POINT-SIZE="13">&#128231; Email Service</FONT></B></FONT></TD></TR>
            <TR><TD ALIGN="LEFT" COLSPAN="2" BGCOLOR="#E8EAF6"><B>SMTP Server</B></TD></TR>
            <TR><TD ALIGN="LEFT" COLSPAN="2" BGCOLOR="#F5F5F5"><B>Features:</B></TD></TR>
            <TR><TD ALIGN="LEFT" COLSPAN="2">&#8226; Email Verification Links</TD></TR>
            <TR><TD ALIGN="LEFT" COLSPAN="2">&#8226; Password Reset Tokens</TD></TR>
            <TR><TD ALIGN="LEFT" COLSPAN="2">&#8226; System Notifications</TD></TR>
        </TABLE>>''', shape='plaintext')

    # Data Flow Connections with enhanced styling
    
    # Frontend to Backend - Primary data flow
    dot.edge('Browser', 'SpringBoot', 
             label='  HTTP/REST API\n  JWT Authentication\n  JSON Payloads  ', 
             color='#1976D2', penwidth='3.0', 
             fontcolor='#1976D2', fontsize='10',
             arrowsize='1.2')

    # Backend to Database - Data persistence
    dot.edge('SpringBoot', 'MySQL', 
             label='  JPA/Hibernate\n  SQL Queries\n  Transactions  ', 
             color='#388E3C', penwidth='3.0',
             fontcolor='#388E3C', fontsize='10',
             arrowsize='1.2')

    # Backend to Redis - Caching layer
    dot.edge('SpringBoot', 'Redis', 
             label='  Caching\n  Rate Limiting\n  Session Store  ', 
             color='#D32F2F', penwidth='2.5',
             fontcolor='#D32F2F', fontsize='10',
             style='dashed', arrowsize='1.2')

    # Backend to Ollama - AI integration
    dot.edge('SpringBoot', 'Ollama', 
             label='  Chat Prompts\n  AI Responses\n  Streaming  ', 
             color='#7B1FA2', penwidth='3.0',
             fontcolor='#7B1FA2', fontsize='10',
             arrowsize='1.2')

    # Backend to Email - Async communication
    dot.edge('SpringBoot', 'Email', 
             label='  SMTP\n  Async Emails\n  Templates  ', 
             color='#F57C00', penwidth='2.5',
             fontcolor='#F57C00', fontsize='10',
             style='dashed', arrowsize='1.2')

    # Add legend with better styling
    with dot.subgraph(name='cluster_legend') as c:
        c.attr(label='Communication Legend', style='filled,rounded', 
               color='#ECEFF1', fillcolor='#ECEFF1',
               fontsize='11', fontname='Segoe UI Bold', 
               penwidth='2', margin='15')
        c.node('legend', '''<<TABLE BORDER="0" CELLBORDER="1" CELLSPACING="0" CELLPADDING="8" BGCOLOR="white">
            <TR><TD ALIGN="LEFT" BGCOLOR="#F5F5F5"><B>Line Type</B></TD><TD ALIGN="LEFT" BGCOLOR="#F5F5F5"><B>Description</B></TD></TR>
            <TR><TD ALIGN="LEFT">Solid Line</TD><TD ALIGN="LEFT">Synchronous Communication</TD></TR>
            <TR><TD ALIGN="LEFT">Dashed Line</TD><TD ALIGN="LEFT">Asynchronous/Caching</TD></TR>
        </TABLE>>''', shape='plaintext')

    return dot


def main():
    """Generate system architecture diagram only"""
    print("🎨 Generating Chatbot System Architecture Diagram...\n")
    
    # Create output directory if it doesn't exist
    output_dir = os.path.join(os.path.dirname(__file__), '..', 'docs/diagrams')
    os.makedirs(output_dir, exist_ok=True)
    
    # Generate Architecture Diagram
    print("🏗️  Generating System Architecture Diagram...")
    arch_diagram = generate_architecture_diagram()
    arch_path = os.path.join(output_dir, 'system_architecture')
    arch_diagram.render(arch_path, cleanup=True)
    print(f"   ✅ Saved: {arch_path}.png")
    
    print(f"\n✨ Architecture diagram generated successfully!")
    print(f"\n📁 Location: {output_dir}")
    print(f"\n📊 File: system_architecture.png")
    print(f"\n💡 This diagram shows:")
    print(f"   • Frontend (React + Vite)")
    print(f"   • Backend (Spring Boot with 12 controllers)")
    print(f"   • Database (MySQL)")
    print(f"   • Cache (Redis)")
    print(f"   • AI (Ollama LLM)")
    print(f"   • External Services (Email)")
    print(f"   • All data flows and connections")


if __name__ == "__main__":
    try:
        main()
    except Exception as e:
        print(f"\n❌ Error: {e}")
        print("\n💡 Make sure you have graphviz installed:")
        print("   pip install graphviz")
        print("   Also install Graphviz system package:")
        print("   - Windows: winget install --id Graphviz.Graphviz -e")
        print("   - Mac: brew install graphviz")
        print("   - Linux: sudo apt-get install graphviz")

