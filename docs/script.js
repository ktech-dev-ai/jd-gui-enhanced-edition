// Scroll Reveal Animation
function reveal() {
    var reveals = document.querySelectorAll(".reveal");

    for (var i = 0; i < reveals.length; i++) {
        var windowHeight = window.innerHeight;
        var elementTop = reveals[i].getBoundingClientRect().top;
        var elementVisible = 100;

        if (elementTop < windowHeight - elementVisible) {
            reveals[i].classList.add("active");
        }
    }
}

window.addEventListener("scroll", reveal);
// Trigger once on load
reveal();

// Optional: Smooth scroll for anchor links
document.querySelectorAll('a[href^="#"]').forEach(anchor => {
    anchor.addEventListener('click', function (e) {
        e.preventDefault();
        document.querySelector(this.getAttribute('href')).scrollIntoView({
            behavior: 'smooth'
        });
    });
});

// Since the version is 1.6.6 currently, the static links in HTML are hardcoded.
// If you want to dynamically fetch the latest release from GitHub API, you could do it here:
/*
async function fetchLatestRelease() {
    try {
        const response = await fetch('https://api.github.com/repos/ktech-dev-ai/jd-gui-enhanced-edition/releases/latest');
        if (response.ok) {
            const data = await response.json();
            // Update download links dynamically based on the assets in the data payload
            console.log("Latest release version: " + data.tag_name);
            // This allows the website to be completely hands-off after publication.
        }
    } catch (e) {
        console.error("Could not fetch latest release", e);
    }
}
fetchLatestRelease();
*/
