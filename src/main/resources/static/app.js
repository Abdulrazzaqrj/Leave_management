
// ==========================================
// GLOBAL VARIABLES
// ==========================================

let currentUser = null;
let allLeaves = [];
let allEmployees = [];

const $ = (selector) => document.querySelector(selector);


// ==========================================
// TOAST MESSAGE
// ==========================================

function toast(message, error = false) {
    const t = $("#toast");

    t.textContent = message;
    t.className = error ? "error" : "";
    t.style.display = "block";

    setTimeout(() => {
        t.style.display = "none";
    }, 3500);
}


// ==========================================
// API FUNCTION
// ==========================================

async function api(url, options = {}) {

    const response = await fetch(url, {
        ...options,
        credentials: "same-origin",
        headers: {
            "Content-Type": "application/json",
            ...(options.headers || {})
        }
    });

    let data = {};

    try {
        data = await response.json();
    } catch {
        // Response may not contain JSON.
    }

    if (!response.ok) {
        throw new Error(
            data.error ||
            data.message ||
            `Request failed: ${response.status}`
        );
    }

    return data;
}


// ==========================================
// HTML ESCAPING
// ==========================================

function escapeHtml(value = "") {

    return String(value).replace(/[&<>"']/g, (char) => {
        return {
            "&": "&amp;",
            "<": "&lt;",
            ">": "&gt;",
            '"': "&quot;",
            "'": "&#39;"
        }[char];
    });
}


// ==========================================
// LOGIN
// ==========================================

async function login(event) {

    event.preventDefault();

    const form = new FormData(event.target);

    const loginData = {
        email: form.get("email"),
        password: form.get("password")
    };

    try {

        const response = await api("/api/auth/login", {
            method: "POST",
            body: JSON.stringify(loginData)
        });

        currentUser = response.user || response;

        if (!currentUser || !currentUser.role) {
            throw new Error("User details not returned by login API.");
        }

        await showApp();

        toast("Login successful!");

    } catch (error) {
        toast(error.message, true);
    }
}


// ==========================================
// SHOW APPLICATION
// ==========================================

async function showApp() {

    $("#login").hidden = true;
    $("#app").hidden = false;

    const isManager = currentUser.role === "MANAGER";

    $("#userArea").innerHTML = `
        <b>${escapeHtml(currentUser.name)}</b>
        · ${escapeHtml(currentUser.role)}
        <button id="logout" type="button">
            Logout
        </button>
    `;

    $("#logout").addEventListener("click", logout);

    // Employee / Manager permissions

    $("#leaveForm").style.display =
        isManager ? "none" : "grid";

    // Show manager-only navigation

    document.querySelectorAll(".manager-only").forEach(item => {
        item.hidden = !isManager;
    });

    // Page titles

    $("#historyTitle").textContent =
        isManager
            ? "All employee leave requests"
            : "My leave history";

    $("#formTitle").textContent =
        isManager
            ? "Manager workspace"
            : "Apply for leave";

    initializeSidebar();

    openPage("dashboard");

    await loadData();
}


// ==========================================
// LOGOUT
// ==========================================

async function logout() {

    try {

        await api("/api/auth/logout", {
            method: "POST"
        });

        currentUser = null;
        allLeaves = [];
        allEmployees = [];

        $("#app").hidden = true;
        $("#login").hidden = false;

        $("#userArea").textContent = "";

        $("#loginForm").reset();

        toast("Logged out successfully.");

    } catch (error) {
        toast(error.message, true);
    }
}


// ==========================================
// RESTORE LOGIN SESSION
// ==========================================

async function checkSession() {

    try {

        const response = await api("/api/auth/me");

        currentUser = response.user || response;

        if (!currentUser || !currentUser.role) {
            throw new Error("Invalid session.");
        }

        await showApp();

    } catch {

        currentUser = null;

        $("#login").hidden = false;
        $("#app").hidden = true;
    }
}


// ==========================================
// SIDEBAR NAVIGATION
// ==========================================

const pageTitles = {

    dashboard: "Dashboard",
    inbox: "Inbox",
    calendar: "Calendar",
    employees: "Employees",
    attendance: "Attendance",
    performance: "Performance",
    payroll: "Payroll",
    leave: "Leave Management"

};


function initializeSidebar() {

    document.querySelectorAll(".nav-link").forEach(link => {

        link.addEventListener("click", event => {

            event.preventDefault();

            const page = link.dataset.page;

            openPage(page);

        });

    });

}


function openPage(page) {

    if (!currentUser) {
        return;
    }

    const isManager = currentUser.role === "MANAGER";

    // Only managers can access Employees page

    if (page === "employees" && !isManager) {

        toast("You do not have permission to access this page.", true);

        return;
    }

    // Hide all pages

    document.querySelectorAll(".page-section").forEach(section => {
        section.hidden = true;
    });

    // Show selected page

    const selectedPage =
        document.querySelector(`[data-section="${page}"]`);

    if (!selectedPage) {
        toast("Page not found.", true);
        return;
    }

    selectedPage.hidden = false;

    // Update active sidebar link

    document.querySelectorAll(".nav-link").forEach(link => {

        link.classList.toggle(
            "active",
            link.dataset.page === page
        );

    });

    // Update header title

    $("#pageTitle").textContent = pageTitles[page];

    $("#breadcrumb").textContent =
        `Dashboard / ${pageTitles[page]}`;
}


// ==========================================
// LOAD DASHBOARD DATA
// ==========================================

async function loadData() {

    try {

        if (currentUser.role === "EMPLOYEE") {

            currentUser = await api("/api/employee/profile");

            allLeaves = await api("/api/employee/leaves");

        } else {

            allLeaves = await api("/api/manager/leaves");

            allEmployees =
                await api("/api/manager/employees");

            renderEmployees(allEmployees);
        }

        render();

        renderInbox();
        renderCalendar();
        renderAttendance();
        renderPerformance();
        renderPayroll();

    } catch (error) {
        toast(error.message, true);
    }
}


// ==========================================
// RENDER EMPLOYEES
// ==========================================

function renderEmployees(employees) {

    $("#employeeRows").innerHTML = employees.map(employee => `

        <tr>

            <td>${escapeHtml(employee.name)}</td>

            <td>${escapeHtml(employee.email)}</td>

            <td>${employee.annualBalance ?? 0}</td>

            <td>${employee.sickBalance ?? 0}</td>

            <td>${employee.otherBalance ?? 0}</td>

        </tr>

    `).join("") || `
        <tr>
            <td colspan="5" class="muted">
                No employees found.
            </td>
        </tr>
    `;
}


// ==========================================
// RENDER DASHBOARD
// ==========================================

function render() {

    const isEmployee = currentUser.role === "EMPLOYEE";

    const balances = isEmployee

        ? [
            ["Annual", currentUser.annualBalance],
            ["Sick", currentUser.sickBalance],
            ["Other", currentUser.otherBalance]
        ]

        : [
            ["Total requests", allLeaves.length],

            ["Pending", allLeaves.filter(
                x => x.status === "PENDING"
            ).length],

            ["Approved", allLeaves.filter(
                x => x.status === "APPROVED"
            ).length],

            ["Rejected", allLeaves.filter(
                x => x.status === "REJECTED"
            ).length]
        ];


    // Summary cards

    $("#summary").innerHTML = balances.map(([name, value]) => `

        <div class="card">

            <small>${escapeHtml(name)}</small>

            <strong>${value ?? 0}</strong>

            <small>
                ${isEmployee ? "days available" : "leave requests"}
            </small>

        </div>

    `).join("");


    // Leave overview

    if (isEmployee) {

        const maximum = {
            Annual: 12,
            Sick: 10,
            Other: 3
        };

        $("#overview").innerHTML = balances.map(([name, value]) => {

            const percentage = Math.min(
                100,
                Math.max(0, (Number(value) / maximum[name]) * 100)
            );

            return `

                <div>

                    <b>${escapeHtml(name)} leave</b>

                    <span style="float:right">
                        ${value ?? 0} days
                    </span>

                    <div class="bar">
                        <span style="width:${percentage}%"></span>
                    </div>

                </div>

            `;

        }).join("");

    } else {

        $("#overview").innerHTML = `

            <p>
                ${allLeaves.filter(
                    x => x.status === "PENDING"
                ).length}
                requests are waiting for your decision.
            </p>

        `;
    }

    // Copy overview to Leave Management page

    $("#leaveOverview").innerHTML =
        $("#overview").innerHTML;

    // Leave history

    renderLeaves();
}


// ==========================================
// RENDER LEAVE HISTORY
// ==========================================

function renderLeaves() {

    const filter = $("#statusFilter").value;

    const isManager = currentUser.role === "MANAGER";

    const filteredLeaves = allLeaves.filter(leave => {

        return filter === "ALL" ||
            leave.status === filter;

    });


    $("#leaveRows").innerHTML = filteredLeaves.map(leave => {

        const actionButtons =
            leave.status === "PENDING" && isManager

                ? `

                    <div class="actions">

                        <button
                            type="button"
                            onclick="decide(${Number(leave.id)}, 'approve')">
                            Approve
                        </button>

                        <button
                            type="button"
                            class="reject"
                            onclick="decide(${Number(leave.id)}, 'reject')">
                            Reject
                        </button>

                    </div>

                `

                : `

                    <span class="status ${escapeHtml(leave.status)}">
                        ${escapeHtml(leave.status)}
                    </span>

                `;


        return `

            <tr>

                <td>${escapeHtml(leave.employeeName)}</td>

                <td>${escapeHtml(leave.type)}</td>

                <td>${escapeHtml(leave.startDate)}</td>

                <td>${escapeHtml(leave.endDate)}</td>

                <td>${leave.days ?? 0}</td>

                <td>${escapeHtml(leave.reason)}</td>

                <td>${actionButtons}</td>

            </tr>

        `;

    }).join("") || `

        <tr>
            <td colspan="7" class="muted">
                No leave requests found.
            </td>
        </tr>

    `;
}


// ==========================================
// INBOX PAGE
// ==========================================

function renderInbox() {

    const isManager = currentUser.role === "MANAGER";

    const inboxLeaves = isManager

        ? allLeaves.filter(x => x.status === "PENDING")

        : allLeaves;


    $("#inboxContent").innerHTML = inboxLeaves.map(leave => `

        <div class="module-item">

            <strong>
                ${escapeHtml(
                    isManager ? leave.employeeName : leave.type + " leave"
                )}
            </strong>

            <small>
                ${escapeHtml(leave.startDate)}
                to
                ${escapeHtml(leave.endDate)}
            </small>

            <p>
                ${escapeHtml(leave.reason)}
            </p>

            <span class="status ${escapeHtml(leave.status)}">
                ${escapeHtml(leave.status)}
            </span>

        </div>

    `).join("") || `
        <p class="muted">No inbox messages available.</p>
    `;
}


// ==========================================
// CALENDAR PAGE
// ==========================================

function renderCalendar() {

    const approvedLeaves = allLeaves
        .filter(x => x.status === "APPROVED")
        .sort((a, b) => a.startDate.localeCompare(b.startDate));


    $("#calendarContent").innerHTML = approvedLeaves.map(leave => `

        <div class="module-item">

            <strong>
                ${escapeHtml(leave.employeeName || leave.type + " leave")}
            </strong>

            <small>
                ${escapeHtml(leave.startDate)}
                to
                ${escapeHtml(leave.endDate)}
            </small>

            <p>
                ${escapeHtml(leave.days)} day(s) approved
            </p>

        </div>

    `).join("") || `
        <p class="muted">No approved leaves available.</p>
    `;
}


// ==========================================
// ATTENDANCE PAGE
// ==========================================

function renderAttendance() {

    const approved = allLeaves.filter(
        x => x.status === "APPROVED"
    );

    const pending = allLeaves.filter(
        x => x.status === "PENDING"
    );

    $("#attendanceContent").innerHTML = `

        <div class="cards">

            <div class="card">
                <small>Approved leave requests</small>
                <strong>${approved.length}</strong>
            </div>

            <div class="card">
                <small>Pending leave requests</small>
                <strong>${pending.length}</strong>
            </div>

            <div class="card">
                <small>Total leave days approved</small>
                <strong>
                    ${approved.reduce(
                        (sum, leave) => sum + Number(leave.days || 0),
                        0
                    )}
                </strong>
            </div>

        </div>

        <p class="muted">
            This overview uses leave request data.
            Attendance records are not yet connected.
        </p>
    `;
}


// ==========================================
// PERFORMANCE PAGE
// ==========================================

function renderPerformance() {

    const total = allLeaves.length;

    const approved = allLeaves.filter(
        x => x.status === "APPROVED"
    ).length;

    const rejected = allLeaves.filter(
        x => x.status === "REJECTED"
    ).length;

    const pending = allLeaves.filter(
        x => x.status === "PENDING"
    ).length;

    const approvalRate = total === 0
        ? 0
        : Math.round((approved / total) * 100);


    $("#performanceContent").innerHTML = `

        <div class="cards">

            <div class="card">
                <small>Total requests</small>
                <strong>${total}</strong>
            </div>

            <div class="card">
                <small>Approved</small>
                <strong>${approved}</strong>
            </div>

            <div class="card">
                <small>Rejected</small>
                <strong>${rejected}</strong>
            </div>

            <div class="card">
                <small>Pending</small>
                <strong>${pending}</strong>
            </div>

        </div>

        <div class="panel">

            <h3>Leave approval rate</h3>

            <strong>${approvalRate}%</strong>

            <div class="bar">
                <span style="width:${approvalRate}%"></span>
            </div>

        </div>

        <p class="muted">
            These statistics describe leave requests,
            not employee job performance.
        </p>
    `;
}


// ==========================================
// PAYROLL PAGE
// ==========================================

function renderPayroll() {

    if (currentUser.role === "EMPLOYEE") {

        $("#payrollContent").innerHTML = `

            <div class="cards">

                <div class="card">
                    <small>Annual leave balance</small>
                    <strong>${currentUser.annualBalance ?? 0}</strong>
                    <small>days</small>
                </div>

                <div class="card">
                    <small>Sick leave balance</small>
                    <strong>${currentUser.sickBalance ?? 0}</strong>
                    <small>days</small>
                </div>

                <div class="card">
                    <small>Other leave balance</small>
                    <strong>${currentUser.otherBalance ?? 0}</strong>
                    <small>days</small>
                </div>

            </div>

            <p class="muted">
                This page currently displays leave balances.
                Salary and payslip details are not yet connected.
            </p>
        `;

    } else {

        $("#payrollContent").innerHTML = `

            <div class="panel">

                <h3>Employee leave balances</h3>

                <p>
                    Total employees: ${allEmployees.length}
                </p>

                <p class="muted">
                    Salary and payslip management
                    are not yet connected.
                </p>

            </div>
        `;
    }
}


// ==========================================
// APPLY FOR LEAVE
// ==========================================

async function applyLeave(event) {

    event.preventDefault();

    const form = new FormData(event.target);

    const leaveData = {

        type: form.get("type"),

        startDate: form.get("startDate"),

        endDate: form.get("endDate"),

        reason: form.get("reason")

    };


    if (leaveData.startDate > leaveData.endDate) {

        toast("End date must be after start date.", true);

        return;
    }


    try {

        await api("/api/employee/leaves", {

            method: "POST",

            body: JSON.stringify(leaveData)

        });

        event.target.reset();

        toast("Leave request submitted successfully.");

        await loadData();

    } catch (error) {

        toast(error.message, true);

    }
}


// ==========================================
// MANAGER APPROVE / REJECT
// ==========================================

async function decide(id, action) {

    const comment = prompt("Optional manager comment:");

    if (comment === null) {
        return;
    }

    try {

        await api(
            `/api/manager/leaves/${id}/${action}`,
            {
                method: "PUT",

                body: JSON.stringify({
                    comment: comment
                })
            }
        );

        toast(
            action === "approve"
                ? "Leave approved successfully."
                : "Leave rejected successfully."
        );

        await loadData();

    } catch (error) {

        toast(error.message, true);

    }
}


// ==========================================
// EVENT LISTENERS
// ==========================================

$("#loginForm").addEventListener("submit", login);

$("#leaveForm").addEventListener("submit", applyLeave);

$("#statusFilter").addEventListener("change", renderLeaves);


// ==========================================
// APPLICATION START
// ==========================================

document.addEventListener("DOMContentLoaded", checkSession);
