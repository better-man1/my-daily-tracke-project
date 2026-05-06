const fs = require('fs');
const https = require('https');

const icons = [
  { name: 'home.png', url: 'https://img.icons8.com/ios-filled/80/64748b/home.png' },
  { name: 'home-active.png', url: 'https://img.icons8.com/ios-filled/80/6366f1/home.png' },
  { name: 'plan.png', url: 'https://img.icons8.com/ios-filled/80/64748b/todo-list.png' },
  { name: 'plan-active.png', url: 'https://img.icons8.com/ios-filled/80/6366f1/todo-list.png' },
  { name: 'accounting.png', url: 'https://img.icons8.com/ios-filled/80/64748b/ledger.png' },
  { name: 'accounting-active.png', url: 'https://img.icons8.com/ios-filled/80/6366f1/ledger.png' },
  { name: 'summary.png', url: 'https://img.icons8.com/ios-filled/80/64748b/graph-report.png' },
  { name: 'summary-active.png', url: 'https://img.icons8.com/ios-filled/80/6366f1/graph-report.png' },
  { name: 'profile.png', url: 'https://img.icons8.com/ios-filled/80/64748b/user.png' },
  { name: 'profile-active.png', url: 'https://img.icons8.com/ios-filled/80/6366f1/user.png' }
];

icons.forEach(icon => {
  const file = fs.createWriteStream(`d:/Projects/ClaudeCodeProject/AAAAA/my-daily-project-claude/miniapp/src/static/tabs/${icon.name}`);
  https.get(icon.url, function(response) {
    response.pipe(file);
    file.on('finish', () => file.close());
  });
});
console.log('Icons downloaded successfully.');
