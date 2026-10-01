let accounts = [];
let selectedAccountNumber = null;
const money = new Intl.NumberFormat('en-IN', { style: 'currency', currency: 'INR' });
const api = async (path, options = {}) => {
  const response = await fetch(`/api${path}`, { headers: { 'Content-Type': 'application/json' }, ...options });
  if (!response.ok) { const error = await response.json().catch(() => ({})); throw new Error(error.message || 'Request failed.'); }
  return response.status === 204 ? null : response.json();
};

async function load() {
  try {
    const [dashboard, accountList] = await Promise.all([api('/dashboard'), api('/accounts')]);
    accounts = accountList;
    document.querySelector('#account-count').textContent = dashboard.accountCount;
    document.querySelector('#total-balance').textContent = money.format(dashboard.totalBalance);
    renderAccounts(); renderTransactions('#recent-transactions', dashboard.recentTransactions);
    fillSelects();
  } catch (error) { notify(error.message); }
}
function renderAccounts() {
  const root = document.querySelector('#accounts');
  root.innerHTML = accounts.length ? accounts.map(account => `<div class="account"><div><div class="account-name">${escapeHtml(account.holderName)}</div><div class="account-number">${account.accountNumber} · ${escapeHtml(account.email)}</div></div><div><div class="balance">${money.format(account.balance)}</div><button class="history-link" onclick="showAccountDetails('${account.accountNumber}')">View details</button></div></div>`).join('') : '<p class="empty">No accounts yet. Open your first account to begin.</p>';
}
function renderTransactions(selector, transactions) {
  const root = document.querySelector(selector);
  root.innerHTML = transactions.length ? transactions.map(transaction => {
    const outgoing = ['WITHDRAWAL', 'TRANSFER_OUT'].includes(transaction.type);
    const label = transaction.type.replace('_', ' ').toLowerCase().replace(/\b\w/g, c => c.toUpperCase());
    const detail = [transaction.accountNumber, transaction.counterpartyAccountNumber && `with ${transaction.counterpartyAccountNumber}`, transaction.reference].filter(Boolean).join(' · ');
    return `<div class="transaction"><div class="transaction-icon">${outgoing ? '↑' : '↓'}</div><div><div class="transaction-name">${label}</div><div class="transaction-meta">${escapeHtml(detail)} · ${new Date(transaction.createdAt).toLocaleString('en-IN')}</div></div><div class="amount ${outgoing ? 'negative' : ''}">${outgoing ? '−' : '+'}${money.format(transaction.amount)}</div></div>`;
  }).join('') : '<p class="empty">No transactions yet.</p>';
}
function fillSelects() {
  const choices = `<option value="" disabled selected>Select an account</option>${accounts.map(account => `<option value="${account.accountNumber}">${escapeHtml(account.holderName)} (${account.accountNumber})</option>`).join('')}`;
  document.querySelectorAll('.account-select').forEach(select => select.innerHTML = choices);
}
function showPanel(id) {
  if (id !== 'create-panel' && !accounts.length) { notify('Open an account before moving money.'); return; }
  document.querySelectorAll('.panel').forEach(panel => panel.classList.remove('active'));
  document.querySelector(`#${id}`).classList.add('active'); document.querySelector('#modal').classList.add('open');
}
function closeModal() { document.querySelector('#modal').classList.remove('open'); }
function outsideClose(event) { if (event.target.id === 'modal') closeModal(); }
async function createAccount(event) {
  event.preventDefault(); const data = Object.fromEntries(new FormData(event.target)); data.openingBalance = Number(data.openingBalance);
  await submit(() => api('/accounts', { method: 'POST', body: JSON.stringify(data) }), 'Account created.');
}
async function moneyAction(event, action) {
  event.preventDefault(); const data = Object.fromEntries(new FormData(event.target)); const number = data.accountNumber; delete data.accountNumber; data.amount = Number(data.amount);
  await submit(() => api(`/accounts/${number}/${action}`, { method: 'POST', body: JSON.stringify(data) }), `${action === 'deposit' ? 'Deposit' : 'Withdrawal'} completed.`);
}
async function transfer(event) {
  event.preventDefault(); const data = Object.fromEntries(new FormData(event.target)); data.amount = Number(data.amount);
  await submit(() => api('/transfers', { method: 'POST', body: JSON.stringify(data) }), 'Transfer completed.');
}
async function submit(request, message) {
  try { await request(); closeModal(); notify(message); await load(); } catch (error) { notify(error.message); }
}
async function showHistory(number) {
  try { const [account, transactions] = await Promise.all([api(`/accounts/${number}`), api(`/accounts/${number}/transactions`)]); document.querySelector('#history-title').textContent = `${account.holderName}'s history`; renderTransactions('#history-list', transactions); showPanel('history-panel'); } catch (error) { notify(error.message); }
}
async function showAccountDetails(number) {
  try {
    const [account, transactions] = await Promise.all([api(`/accounts/${number}`), api(`/accounts/${number}/transactions`)]);
    selectedAccountNumber = account.accountNumber;
    document.querySelector('#details-name').textContent = account.holderName;
    document.querySelector('#account-details').innerHTML = `<div style="display:flex;justify-content:space-between;align-items:center;padding:17px 0;border-top:1px solid var(--line);border-bottom:1px solid var(--line)"><span style="color:var(--muted);font-size:13px">Available balance</span><strong style="font-size:24px">${money.format(account.balance)}</strong></div><div style="display:grid;gap:10px;padding:16px 0;font-size:13px"><div><span style="color:var(--muted)">Account number</span><br>${account.accountNumber}</div><div><span style="color:var(--muted)">Email address</span><br>${escapeHtml(account.email)}</div><div><span style="color:var(--muted)">Opened</span><br>${new Date(account.createdAt).toLocaleDateString('en-IN', { day: 'numeric', month: 'long', year: 'numeric' })}</div></div>`;
    renderTransactions('#details-transactions', transactions.slice(0, 5));
    showPanel('details-panel');
  } catch (error) { notify(error.message); }
}
function openMoneyPanel(action) { showPanel(`${action}-panel`); document.querySelector(`#${action}-panel .account-select`).value = selectedAccountNumber; }
function openTransferPanel() { showPanel('transfer-panel'); document.querySelector('#transfer-panel select[name="fromAccountNumber"]').value = selectedAccountNumber; }
function notify(message) { const toast = document.querySelector('#toast'); toast.textContent = message; toast.classList.add('show'); clearTimeout(window.toastTimer); window.toastTimer = setTimeout(() => toast.classList.remove('show'), 3400); }
function escapeHtml(value = '') { const node = document.createElement('span'); node.textContent = value; return node.innerHTML; }
load();
