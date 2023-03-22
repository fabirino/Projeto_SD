file1 = open("./english.txt", "r")

lines = file1.readlines()
string = "{"
for line in lines:
    string += "\"" + line[0:-1] + "\"" + ","

string = string[0:-1]
string += "}"

file2 = open("./output.txt", "w")
file2.write(string)

file1.close()
file2.close()