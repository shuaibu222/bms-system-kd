document.addEventListener("DOMContentLoaded", function () {
    const sidebar = document.querySelector(".sidebar");
    const sidebarToggle = document.getElementById("sidebar-toggle");
    const overlay = document.createElement("div");
    overlay.classList.add("overlay");
    document.body.appendChild(overlay);

    // Toggle sidebar on tablet size
    sidebarToggle.addEventListener("click", function () {
        sidebar.classList.toggle("open");
        overlay.style.display = sidebar.classList.contains("open") ? "block" : "none";
    });

    // Close sidebar when overlay is clicked
    overlay.addEventListener("click", function () {
        sidebar.classList.remove("open");
        overlay.style.display = "none";
    });

    // Handle window resize for responsive behavior
    window.addEventListener("resize", function () {
        if (window.innerWidth < 768) {
            sidebar.classList.remove("open"); // Ensure sidebar is hidden on small screens
            overlay.style.display = "none"; // Hide overlay on small screens
        } else {
            sidebar.classList.remove("open"); // Ensure sidebar is visible on larger screens
            overlay.style.display = "none"; // Hide overlay on larger screens
        }
    });

    // Initial check for responsive behavior
    if (window.innerWidth < 768) {
        sidebar.classList.remove("open"); // Hide sidebar on small screens initially
    }
});

document.addEventListener("DOMContentLoaded", function () {
    const currentUrl = window.location.pathname; // Get the current URL path

    // Function to add the 'active' class to matching elements
    function setActiveNavItem() {
        // Check the dashboard link
        const dashboardLink = document.querySelector('.dashboard');
        if (dashboardLink && dashboardLink.getAttribute('href') === currentUrl) {
            dashboardLink.classList.add('active');
        }

        // Check all nav items with submenus
        const navItems = document.querySelectorAll('.nav-item');
        navItems.forEach(item => {
            const submenuLinks = item.querySelectorAll('.submenu a');
            let isActive = false;

            // Check if any submenu link matches the current URL
            submenuLinks.forEach(link => {
                if (link.getAttribute('href') === currentUrl) {
                    isActive = true;
                }
            });

            // If a submenu link matches, add the 'active' class to the parent nav-item
            if (isActive) {
                item.classList.add('active');
            }
        });
    }

    // Call the function to set the active state
    setActiveNavItem();
});