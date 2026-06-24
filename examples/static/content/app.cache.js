const statusNode = document.querySelector("[data-status]");
const button = document.querySelector("[data-refresh]");

function refreshStatus() {
  const now = new Date().toLocaleTimeString();
  statusNode.textContent = `Static assets loaded at ${now}`;
}

button.addEventListener("click", refreshStatus);
refreshStatus();
