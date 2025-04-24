import os, json, re

# Load the full webhook payload
with open(os.environ['GITHUB_EVENT_PATH'], 'r') as f:
    event = json.load(f)

pr = event.get('pull_request', {})
number = pr.get('number')
title = pr.get('title', '').strip()
body = pr.get('body', '') or ''

# Extract changelog block after ":cl:" up to "---" or "-->"
m = re.search(r':cl:(.*?)(?:\n---|\n-->)', body, re.S)
if m:
    # split into non-empty lines, strip whitespace
    entries = [line.strip() for line in m.group(1).splitlines() if line.strip()]
else:
    # fallback to PR title
    entries = [title]

# Format entries
repo = os.environ['GITHUB_REPOSITORY']
link = f'https://github.com/{repo}/pull/{number}'
lines = [f"- {e} ([#{number}]({link}))" for e in entries]

# Append to CHANGELOG.md
with open('CHANGELOG.md', 'a') as fh:
    fh.write('\n'.join(lines) + '\n')
