
import os

file_path = r'src\com\vnengine\ui\GameWindow.java'

with open(file_path, 'r', encoding='utf-8') as f:
    lines = f.readlines()

content = "".join(lines)
search_str = "private void drawSaveLoadContent"

first_idx = content.find(search_str)
if first_idx == -1:
    print("No drawSaveLoadContent found!")
    exit(1)

second_idx = content.find(search_str, first_idx + 1)
if second_idx == -1:
    print("No second drawSaveLoadContent found! Matches my view?")
    # Check if we should find it by lines
    # previous view Step 89 said line 1014.
    # Let's trust grep/find failed and I relying on view.
    exit(1)

print(f"Found duplicate at index {second_idx}")

# Find start of line for second_idx
start_line_idx = content.rfind('\n', 0, second_idx) + 1

# Now parse braces to find end
brace_count = 0
found_brace = False
end_idx = -1

for i in range(second_idx, len(content)):
    char = content[i]
    if char == '{':
        brace_count += 1
        found_brace = True
    elif char == '}':
        brace_count -= 1
        if found_brace and brace_count == 0:
            end_idx = i + 1 # Include the brace
            break

if end_idx != -1:
    print(f"Removing from {start_line_idx} to {end_idx}")
    new_content = content[:start_line_idx] + content[end_idx:]
    # Also strip potential trailing newline/indentation left behind if we cut cleanly?
    
    with open(file_path, 'w', encoding='utf-8') as f:
        f.write(new_content)
    print("Fixed!")
else:
    print("Could not find closing brace")
