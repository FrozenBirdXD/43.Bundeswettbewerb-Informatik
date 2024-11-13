import matplotlib.pyplot as plt

segments_read = []

with open('krocket/beispielaufgaben/krocket1.txt', 'r') as file:
    next(file)
    for line in file:
        segment = list(map(int, line.split()))
        segments_read.append(segment)

plt.figure(figsize=(8, 6))

# Plot each line segment
for segment in segments_read:
    x1, y1, x2, y2 = segment
    plt.plot([x1, x2], [y1, y2], marker='o') 

plt.xlabel("X-axis")
plt.ylabel("Y-axis")
plt.title("Krocket Gates")
plt.grid(True)

plt.show()
