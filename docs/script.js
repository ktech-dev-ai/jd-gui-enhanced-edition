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

// Dynamically fetch the latest release from GitHub API
async function fetchLatestRelease() {
    try {
        const response = await fetch('https://api.github.com/repos/ktech-dev-ai/jd-gui-enhanced-edition/releases/latest');
        if (response.ok) {
            const data = await response.json();
            const version = data.tag_name.replace('v', ''); // e.g. "2.0.0"
            
            // Generate the dynamic download links
            const baseUrl = `https://github.com/ktech-dev-ai/jd-gui-enhanced-edition/releases/latest/download`;
            
            const winBtn = document.getElementById('win-download');
            const macBtn = document.getElementById('mac-download');
            const jarBtn = document.getElementById('jar-download');
            
            if (winBtn) winBtn.href = `${baseUrl}/jd-gui-enhanced-edition.exe`;
            if (macBtn) macBtn.href = `${baseUrl}/jd-gui-osx-${version}.tar`;
            if (jarBtn) jarBtn.href = `${baseUrl}/jd-gui-${version}.jar`;
            
            console.log("Successfully fetched latest release version: " + version);
        }
    } catch (e) {
        console.error("Could not fetch latest release", e);
    }
}
fetchLatestRelease();
