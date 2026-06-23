const statusNode = document.querySelector("[data-status]");
const viewsNode = document.querySelector("[data-views]");
const button = document.querySelector("[data-refresh]");

async function refreshStatus() {
  const now = new Date().toLocaleTimeString();
  statusNode.textContent = `Static assets loaded at ${now}`;
  const response = await fetch("/views");
  const summary = await response.json();
  viewsNode.textContent = `${summary.count} static file responses have been persisted.`;
}

button.addEventListener("click", refreshStatus);
refreshStatus();
