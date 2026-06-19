import re

file_path = "/Users/tonyaloysius/Documents/Itinera/shared/src/commonMain/kotlin/com/itinera/app/i18n/Strings.kt"
with open(file_path, 'r') as f:
    content = f.read()

# Extract data class fields
data_class_match = re.search(r'data class Strings\((.*?)\)', content, re.DOTALL)
fields = []
if data_class_match:
    fields_content = data_class_match.group(1)
    fields = re.findall(r'val\s+(\w+)\s*:', fields_content)

# Extract EN assignments
en_match = re.search(r'private val EN = Strings\((.*?)\)', content, re.DOTALL)
assignments = []
if en_match:
    en_content = en_match.group(1)
    assignments = re.findall(r'(\w+)\s*=', en_content)

print(f"Data Class Fields ({len(fields)}):")
print(sorted(fields))
print(f"EN Assignments ({len(assignments)}):")
print(sorted(assignments))

print("\nFields in Data Class but NOT in EN:")
print(set(fields) - set(assignments))

print("\nAssignments in EN but NOT in Data Class:")
print(set(assignments) - set(fields))
