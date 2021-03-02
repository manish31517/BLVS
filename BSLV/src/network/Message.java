package network;


import java.io.Serializable;

/**
 * MessageStruct
 *
 * A structure for communicating between server and client.
 * Two field indicate the message type(_code) and message body(_content)
 * Message types and description:
 *
 * type                 description                 direction
 * 0                  server broadcasts block      server->client
 * 1                  client sends block           client->server
 * 2                  server sends clientid        server->client
 * */
public class Message extends Object implements Serializable {
    private static  final long serialVersionUID =  3532734764930998421L;
    public int  code;
    public Object content;
    public Message(){
        this.code=0;
        this.content=null;
    }
    public Message(int code,Object content){
        this.code =code;
        this.content=content;
    }
}
