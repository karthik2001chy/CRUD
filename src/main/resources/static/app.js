// Student Management System - Application JavaScript
// This file contains all the JavaScript functionality for the frontend

const API_BASE_URL = 'http://localhost:8085/api';
let currentPage = 0;
let totalPages = 0;
let currentPageSize = 10;

// ============================================
// Section Switching
// ============================================

/**
 * Switch between dashboard, register, simple view, and full view sections
 * @param {string} sectionId - The ID of the section to display
 */
function switchSection(sectionId) {
    // Hide all sections
    document.querySelectorAll('.section').forEach(section => {
        section.classList.remove('active');
    });

    // Remove active class from all buttons
    document.querySelectorAll('.nav-btn').forEach(btn => {
        btn.classList.remove('active');
    });

    // Show selected section
    document.getElementById(sectionId).classList.add('active');

    // Add active class to clicked button
    event.target.classList.add('active');

    // Load data for specific sections
    if (sectionId === 'dashboard') {
        loadDashboard();
    } else if (sectionId === 'search') {
        loadSearchPage();
    } else if (sectionId === 'view-full') {
        loadFullStudents(0);
    }
}

// ============================================
// Dashboard Functions
// ============================================

/**
 * Load dashboard with statistics and recent students
 */
async function loadDashboard() {
    try {
        const response = await fetch(`${API_BASE_URL}/students?page=0&size=5`);
        const data = await response.json();

        // Load statistics
        const statsContainer = document.getElementById('dashboardStats');
        statsContainer.innerHTML = `
            <div class="student-card">
                <h3>📊 Total Students</h3>
                <p style="font-size: 2em; color: #667eea; font-weight: bold;">${data.totalElements || 0}</p>
            </div>
            <div class="student-card">
                <h3>📄 Total Pages</h3>
                <p style="font-size: 2em; color: #667eea; font-weight: bold;">${data.totalPages || 0}</p>
            </div>
            <div class="student-card">
                <h3>👥 Current Page</h3>
                <p style="font-size: 2em; color: #667eea; font-weight: bold;">${(data.number || 0) + 1}</p>
            </div>
            <div class="student-card">
                <h3>📦 Items Per Page</h3>
                <p style="font-size: 2em; color: #667eea; font-weight: bold;">${data.size || 0}</p>
            </div>
        `;

        // Load recent students
        const recentContainer = document.getElementById('recentStudents');
        if (data.content && data.content.length > 0) {
            recentContainer.innerHTML = data.content.map(student => `
                <div class="student-card">
                    <h3>${student.name}</h3>
                    <p><span class="label">Reg No:</span> ${student.registrationNo}</p>
                    <p><span class="label">Father:</span> ${student.fatherName}</p>
                    <p><span class="label">Mother:</span> ${student.motherName}</p>
                    <button class="btn btn-view" onclick="viewStudentDetails('${student.registrationNo}')">View Details</button>
                </div>
            `).join('');
        } else {
            recentContainer.innerHTML = '<div class="empty-state"><div class="empty-state-icon">📭</div><p>No students found</p></div>';
        }
    } catch (error) {
        console.error('Error loading dashboard:', error);
        showAlert('Error loading dashboard', 'error');
    }
}

// ============================================
// Registration Functions
// ============================================

/**
 * Handle student registration form submission
 * @param {Event} event - The form submission event
 */
async function handleRegisterStudent(event) {
    event.preventDefault();

    const studentData = {
        registrationNo: document.getElementById('regNo').value,
        name: document.getElementById('name').value,
        dateOfBirth: document.getElementById('dob').value,
        address: document.getElementById('address').value,
        fatherName: document.getElementById('fatherName').value,
        fatherContactNo: document.getElementById('fatherContact').value,
        motherName: document.getElementById('motherName').value,
        motherContactNo: document.getElementById('motherContact').value
    };

    try {
        const response = await fetch(`${API_BASE_URL}/students`, {
            method: 'POST',
            headers: {
                'Content-Type': 'application/json'
            },
            body: JSON.stringify(studentData)
        });

        if (response.ok) {
            showAlert('✅ Student registered successfully!', 'success');
            document.getElementById('registerForm').reset();
            setTimeout(() => {
                switchSection('view-simple');
                document.querySelector('[onclick="switchSection(\'view-simple\')"]').click();
            }, 1500);
        } else {
            showAlert('❌ Failed to register student', 'error');
        }
    } catch (error) {
        console.error('Error registering student:', error);
        showAlert('Error registering student', 'error');
    }
}

// ============================================
// Search Student by Name Functions
// ============================================

/**
 * Load search page
 */
function loadSearchPage() {
    // Page is already loaded, just make sure it's ready
    document.getElementById('searchContent').style.display = 'none';
    document.getElementById('searchResults').innerHTML = '';
}

/**
 * Search for students by name
 */
async function searchStudentByName() {
    const searchInput = document.getElementById('searchNameInput').value.trim();

    if (!searchInput) {
        showAlert('⚠️ Please enter a name to search', 'info');
        return;
    }

    const loading = document.getElementById('searchLoading');
    const content = document.getElementById('searchContent');
    const resultsContainer = document.getElementById('searchResults');

    loading.classList.add('active');
    content.style.display = 'none';

    try {
        const response = await fetch(`${API_BASE_URL}/students/search/name?name=${encodeURIComponent(searchInput)}`);

        if (!response.ok) {
            throw new Error('Search failed');
        }

        const students = await response.json();

        if (students.length > 0) {
            resultsContainer.innerHTML = `
                <div style="grid-column: 1/-1; margin-bottom: 20px;">
                    <p style="color: #667eea; font-weight: 600; font-size: 1.1em;">
                        ✅ Found ${students.length} student(s) matching "${searchInput}"
                    </p>
                </div>
            ` + students.map(student => `
                <div class="student-card">
                    <h3>${student.name}</h3>
                    <p><span class="label">📍 Reg No:</span> <span class="badge">${student.registrationNo}</span></p>
                    <p><span class="label">📅 DOB:</span> ${formatDate(student.dateOfBirth)}</p>
                    <p><span class="label">📍 Address:</span> ${student.address}</p>
                    <p><span class="label">👨 Father:</span> ${student.fatherName}</p>
                    <p><span class="label">📞 Father Contact:</span> ${student.fatherContactNo}</p>
                    <p><span class="label">👩 Mother:</span> ${student.motherName}</p>
                    <p><span class="label">📞 Mother Contact:</span> ${student.motherContactNo}</p>
                    <div class="actions">
                        <button class="btn btn-view" onclick="viewStudentDetails('${student.registrationNo}')">View Details</button>
                        <button class="btn btn-delete" onclick="deleteStudent('${student.registrationNo}')">Delete</button>
                    </div>
                </div>
            `).join('');
        } else {
            resultsContainer.innerHTML = `
                <div class="empty-state" style="grid-column: 1/-1;">
                    <div class="empty-state-icon">🔍</div>
                    <p>No students found with name containing "${searchInput}"</p>
                    <p style="font-size: 0.9em; color: #999; margin-top: 10px;">Try searching with different keywords</p>
                </div>
            `;
        }

        loading.classList.remove('active');
        content.style.display = 'block';
    } catch (error) {
        console.error('Error searching students:', error);
        loading.classList.remove('active');
        showAlert('Error searching students. Please try again.', 'error');
    }
}

/**
 * Clear search results
 */
function clearSearchResults() {
    document.getElementById('searchNameInput').value = '';
    document.getElementById('searchContent').style.display = 'none';
    document.getElementById('searchResults').innerHTML = '';
    showAlert('🧹 Search cleared', 'info');
}

/**
 * Allow search when pressing Enter key
 */
document.addEventListener('DOMContentLoaded', function() {
    const searchInput = document.getElementById('searchNameInput');
    if (searchInput) {
        searchInput.addEventListener('keypress', function(event) {
            if (event.key === 'Enter') {
                searchStudentByName();
            }
        });
    }
});

// ============================================
// Full View Functions
// ============================================


/**
 * Load and display full student view with pagination
 * @param {number} page - The page number to load
 */
async function loadFullStudents(page) {
    const loading = document.getElementById('fullLoading');
    const content = document.getElementById('fullContent');
    const pageSize = parseInt(document.getElementById('pageSize').value) || 10;
    currentPageSize = pageSize;

    loading.classList.add('active');
    content.style.display = 'none';

    try {
        const response = await fetch(`${API_BASE_URL}/students?page=${page}&size=${pageSize}`);
        const data = await response.json();

        currentPage = page;
        totalPages = data.totalPages;

        const tbody = document.getElementById('studentsTableBody');
        if (data.content && data.content.length > 0) {
            tbody.innerHTML = data.content.map(student => `
                <tr>
                    <td><span class="badge">${student.registrationNo}</span></td>
                    <td>${student.name}</td>
                    <td>${student.fatherName}</td>
                    <td>${student.motherName}</td>
                    <td>${student.address}</td>
                    <td>${formatDate(student.dateOfBirth)}</td>
                    <td>
                        <button class="btn btn-view" onclick="viewStudentDetails('${student.registrationNo}')">View</button>
                        <button class="btn btn-delete" onclick="deleteStudent('${student.registrationNo}')">Delete</button>
                    </td>
                </tr>
            `).join('');
        } else {
            tbody.innerHTML = '<tr><td colspan="7" style="text-align: center; padding: 40px;">No students found</td></tr>';
        }

        // Render pagination controls
        const paginationDiv = document.getElementById('pagination');
        let paginationHTML = '';

        if (page > 0) {
            paginationHTML += `<button onclick="loadFullStudents(${page - 1})">← Previous</button>`;
        }

        for (let i = Math.max(0, page - 2); i < Math.min(totalPages, page + 3); i++) {
            paginationHTML += `<button class="${i === page ? 'active' : ''}" onclick="loadFullStudents(${i})">${i + 1}</button>`;
        }

        if (page < totalPages - 1) {
            paginationHTML += `<button onclick="loadFullStudents(${page + 1})">Next →</button>`;
        }

        paginationDiv.innerHTML = paginationHTML;

        loading.classList.remove('active');
        content.style.display = 'block';
    } catch (error) {
        console.error('Error loading students:', error);
        loading.classList.remove('active');
        showAlert('Error loading students', 'error');
    }
}

/**
 * Search students by name in full view
 */
async function searchFullViewByName() {
    const searchInput = document.getElementById('fullSearchInput').value.trim();

    if (!searchInput) {
        showAlert('⚠️ Please enter a name to search', 'info');
        return;
    }

    const loading = document.getElementById('fullLoading');
    const content = document.getElementById('fullContent');
    const tbody = document.getElementById('studentsTableBody');

    loading.classList.add('active');
    content.style.display = 'none';

    try {
        const response = await fetch(`${API_BASE_URL}/students/search/name?name=${encodeURIComponent(searchInput)}`);

        if (!response.ok) {
            throw new Error('Search failed');
        }

        const students = await response.json();

        if (students.length > 0) {
            tbody.innerHTML = students.map(student => `
                <tr>
                    <td><span class="badge">${student.registrationNo}</span></td>
                    <td>${student.name}</td>
                    <td>${student.fatherName}</td>
                    <td>${student.motherName}</td>
                    <td>${student.address}</td>
                    <td>${formatDate(student.dateOfBirth)}</td>
                    <td>
                        <button class="btn btn-view" onclick="viewStudentDetails('${student.registrationNo}')">View</button>
                        <button class="btn btn-delete" onclick="deleteStudent('${student.registrationNo}')">Delete</button>
                    </td>
                </tr>
            `).join('');
        } else {
            tbody.innerHTML = '<tr><td colspan="7" style="text-align: center; padding: 40px;">No students found matching "' + searchInput + '"</td></tr>';
        }

        // Hide pagination for search results
        document.getElementById('pagination').innerHTML = '';

        loading.classList.remove('active');
        content.style.display = 'block';
    } catch (error) {
        console.error('Error searching students:', error);
        loading.classList.remove('active');
        showAlert('Error searching students', 'error');
    }
}

// ============================================
// Modal Functions
// ============================================

/**
 * View student details in a modal popup
 * @param {string} registrationNo - Student registration number
 */
async function viewStudentDetails(registrationNo) {
    try {
        const response = await fetch(`${API_BASE_URL}/students/full/${registrationNo}`);
        if (!response.ok) {
            showAlert('Student not found', 'error');
            return;
        }

        const student = await response.json();
        const modalBody = document.getElementById('modalBody');

        modalBody.innerHTML = `
            <div class="detail-grid">
                <div class="detail-item">
                    <div class="label">Registration Number</div>
                    <div class="value">${student.registrationNo}</div>
                </div>
                <div class="detail-item">
                    <div class="label">Full Name</div>
                    <div class="value">${student.name}</div>
                </div>
                <div class="detail-item">
                    <div class="label">Date of Birth</div>
                    <div class="value">${formatDate(student.dateOfBirth)}</div>
                </div>
                <div class="detail-item">
                    <div class="label">Address</div>
                    <div class="value">${student.address}</div>
                </div>
                <div class="detail-item">
                    <div class="label">Father's Name</div>
                    <div class="value">${student.fatherName}</div>
                </div>
                <div class="detail-item">
                    <div class="label">Father's Contact</div>
                    <div class="value">${student.fatherContactNo}</div>
                </div>
                <div class="detail-item">
                    <div class="label">Mother's Name</div>
                    <div class="value">${student.motherName}</div>
                </div>
                <div class="detail-item">
                    <div class="label">Mother's Contact</div>
                    <div class="value">${student.motherContactNo}</div>
                </div>
            </div>
            <div style="text-align: right;">
                <button class="btn btn-delete" onclick="deleteStudent('${student.registrationNo}')">Delete Student</button>
                <button class="btn btn-secondary" onclick="closeModal()">Close</button>
            </div>
        `;

        document.getElementById('detailsModal').classList.add('active');
    } catch (error) {
        console.error('Error fetching student details:', error);
        showAlert('Error loading student details', 'error');
    }
}

/**
 * Close the details modal
 */
function closeModal() {
    document.getElementById('detailsModal').classList.remove('active');
}

// ============================================
// Delete Functions
// ============================================

/**
 * Delete a student with confirmation
 * @param {string} registrationNo - Student registration number
 */
async function deleteStudent(registrationNo) {
    if (!confirm('Are you sure you want to delete this student?')) {
        return;
    }

    try {
        const response = await fetch(`${API_BASE_URL}/students/${registrationNo}`, {
            method: 'DELETE'
        });

        if (response.ok) {
            showAlert('✅ Student deleted successfully!', 'success');
            closeModal();
            // Reload the current view
            const activeSection = document.querySelector('.section.active').id;
            if (activeSection === 'search') {
                // Search page will stay as is, user can search again if needed
            } else if (activeSection === 'view-full') {
                loadFullStudents(0);
            }
        } else {
            showAlert('Failed to delete student', 'error');
        }
    } catch (error) {
        console.error('Error deleting student:', error);
        showAlert('Error deleting student', 'error');
    }
}

// ============================================
// Utility Functions
// ============================================

/**
 * Display an alert message
 * @param {string} message - The message to display
 * @param {string} type - The type of alert (success, error, info)
 */
function showAlert(message, type) {
    const alertDiv = document.createElement('div');
    alertDiv.className = `alert alert-${type}`;
    alertDiv.textContent = message;

    const container = document.querySelector('.content');
    container.insertBefore(alertDiv, container.firstChild);

    // Auto-dismiss after 5 seconds
    setTimeout(() => {
        alertDiv.remove();
    }, 5000);
}

/**
 * Format a date string for display
 * @param {string} dateString - The date string to format
 * @returns {string} - Formatted date
 */
function formatDate(dateString) {
    if (!dateString) return 'N/A';
    const options = { year: 'numeric', month: 'long', day: 'numeric' };
    return new Date(dateString).toLocaleDateString('en-US', options);
}

// ============================================
// Event Listeners
// ============================================

/**
 * Close modal when clicking outside of it
 */
document.addEventListener('click', (e) => {
    const modal = document.getElementById('detailsModal');
    if (e.target === modal) {
        closeModal();
    }
});

/**
 * Load dashboard when page loads
 */
window.addEventListener('load', loadDashboard);

