import matplotlib.pyplot as plt

segments_read = []

with open('./Quelltext/beispielaufgaben/krocket5.txt', 'r') as file:
    next(file)
    for line in file:
        segment = list(map(int, line.split()))
        segments_read.append(segment)

plt.figure(figsize=(8, 6))

# Plot each line segment
for segment in segments_read:
    x1, y1, x2, y2 = segment
    plt.plot([x1, x2], [y1, y2], marker='o') 
#
#
# Chose points, that result line should intersect
#
#
P1 = (1314.4010000000007,14678.04)
P2 = (3160.4020000000005,14215.655999999999) 


m = (P2[1] - P1[1]) / (P2[0] - P1[0])

#
#
# Extend the x value with the addidion to show more of the line or less
#
#
x_values = [P1[0], P2[0] + 48000]  # Extend the x value
y_values = [m * (x - P1[0]) + P1[1] for x in x_values]

plt.plot(x_values, y_values, color='red', label='Gerade durch P1 und P2', marker='x')

plt.xlabel("X-axis")
plt.ylabel("Y-axis")
plt.title("Krocket Gates")
plt.grid(True)

plt.show()
