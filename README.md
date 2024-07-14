# Network-based Gwent Game

- You can download jars from: [Client Jars](https://s32.picofile.com/file/8477617226/gwent_client_jars.zip.html)  /  [Server Jars](https://s32.picofile.com/file/8477617450/gwent_server_jars.zip.html)
- You must run the server jar (at least jre 21) as:
 
    ```
    java -jar gwent-server.jar <server-ip>:<port> <email-server>:<port>
    ```
    which the email-server is the server that runs `sendmail.py` python script for sending mails.

    (Note: your http port for links will be <server-port> + 1)


- You must run the client jar (at least jre 21) as:
    ```
    java -jar gwent-client.jar <server-ip>:<port>
    ```
- For local testing, Please copy users.db to your ~/gwent-data and Copy jwt.txt to ~/.gwentdata


# Authors
- Amirmahdi Tahmasebi: 402106178
- AmirHossein MohammadZadeh: 402106434
- Parsa Pordeli Behrouz: 402111118

# Special Thanks
- Boss (for help with rebasing)

# Finish Date
The development of this Project finished at 1403/04/21  :)
