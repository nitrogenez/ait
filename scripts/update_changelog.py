import os, json, re, argparse
import requests

parser = argparse.ArgumentParser()
parser.add_argument(
    "--pr-numbers", default="", help="Comma-separated PR numbers to process"
)
args = parser.parse_args()

repo = os.environ["GITHUB_REPOSITORY"]
token = os.environ["BOT_TOKEN"]
headers = {"Authorization": f"Bearer {token}", "Accept": "application/vnd.github.v3+json"}

def fetch_pr(pr_number):
    url = f"https://api.github.com/repos/{repo}/pulls/{pr_number}"
    r = requests.get(url, headers=headers)
    r.raise_for_status()
    return r.json()

def extract_entries(title, body):
    m = re.search(r":cl:(.*?)(?:\n---|\n-->)", body or "", re.S)
    if m:
        return [ln.strip() for ln in m.group(1).splitlines() if ln.strip()]
    return [title]

def process_pr(pr):
    num = pr["number"]
    title = pr["title"].strip()
    body = pr.get("body") or ""
    entries = extract_entries(title, body)
    link = f"https://github.com/{repo}/pull/{num}"
    return [f"- {e} ([#{num}]({link}))" for e in entries]

all_lines = []

if args.pr_numbers:
    for n in args.pr_numbers.split(","):
        pr = fetch_pr(n.strip())
        all_lines.extend(process_pr(pr))
else:
    with open(os.environ["GITHUB_EVENT_PATH"]) as f:
        ev = json.load(f)
    pr = ev["pull_request"]
    all_lines = process_pr(pr)

with open("CHANGELOG.md", "a") as fh:
    fh.write("\n".join(all_lines) + "\n")
