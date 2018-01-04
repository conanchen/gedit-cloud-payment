[https://docs.docker.com/get-started/part2/#run-the-app]
1. Build the local app image 
    - $ docker build -t gedit-cloud-hello .
    - $ docker run -p 8088:8088 -p 8980:8980 gedit-cloud-hello
2. [https://docs.docker.com/get-started/part2/#share-your-image] 
3. Pull and run the image from the remote repository
	- $ docker run -p 8088:8088 -p 8980:8980 conanchen/gedit-cloud-hello:latest


- skip test
  - $ gradle build -x test
  - $  
  
  