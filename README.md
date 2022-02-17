# MDP-Android
 MDP Android

This is a project done for CZ3004 Multi Disciplinary Project. The objective of this project is for teams (comprising of students from Comp Sci. and Comp. Eng) to come together to design a robot that can tackle challenges like Fastest path, image recognition and exploration in a huge square grid board.
 
There are 4 sub components: (1) Arduino robot (2) Algorithm (3) RPI [Middleman communicating with all sub components] (4) Android

This repo is for the Android part that I am solely in charge of. Android's team responsibilities are to display all the real time updates of the robo through bluetooth connection (Arduino -> RPI -> Android) for the leaderboard challenges and manual control of the robot for debugging during normal lab sessions.

Features:
1) Scan bluetooth device
2) Make bluetooth visible
3) Send message/Receive message from/to bluetooth device
4) Scan for bluetooth devices in the area
5) Auto reconnect bluetooth to the last connection
6) Displays robot updates on the map
7) Advanced feature: Voice recognition simple commands (E.g move forward ,rotate left,etc)
8) Manual control of the robot (Move forward, turn left and right)
9) Save 2 user configurable MDF strings (Persistent storage)
10) Display MDF string (Explored grids, obstacles detected, unexplored grids)
11) Display Image string (Detected image ID, X Coord, Y Coord)
12) Auto update toggle switch
13) Manual refresh the map
14) Message history

![Screenshot_20210301-210148](https://user-images.githubusercontent.com/16291759/154525953-b97d1ca8-9ec9-49ca-a7ff-ceab3b96efd3.jpg)
![Screenshot_20210301-210200](https://user-images.githubusercontent.com/16291759/154525982-d941eabc-f2bf-4eb7-b925-bc57f7ee169b.jpg)
![Screenshot_20210301-210203](https://user-images.githubusercontent.com/16291759/154525991-f1093c93-bc27-4abc-b030-8afdd74689d5.jpg)
![Screenshot_20210301-210227](https://user-images.githubusercontent.com/16291759/154525996-797a95a6-94f3-406f-9f23-99092110000d.jpg)
![Screenshot_20210301-210409](https://user-images.githubusercontent.com/16291759/154526002-0b3666c6-25c1-4811-992e-e86c6f4e7469.jpg)
![Screenshot_20210310-204723](https://user-images.githubusercontent.com/16291759/154526013-58efe98d-5913-4d5b-9a92-33963ef18fb2.jpg)
