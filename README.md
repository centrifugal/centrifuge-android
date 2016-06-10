# centrifuge-android
Centrifugo android client

### Usage

##### Create client and connect:
 ```java
 String centrifugoAddress = "wss://centrifugo.herokuapp.com/connection/websocket";
 String userId = "...";
 String userToken = "..."; //nullable
 String token = "...";
 String tokenTimestamp = "...";
 Centrifugo centrifugo = new Centrifugo.Builder(centrifugoAddress)
                         .setUser(new User(userId, userToken))
                         .setToken(new Token(token, tokenTimestamp))
                         .build();
 centrifugo.connect();
 ```
##### Subscribe to channel:
```java
String channel = "my-channel";
centrifugo.subscribe(new SubscriptionRequest(channel));
```
##### Listen to events:
###### Connection events
```java
centrifugo.setConnectionListener(new ConnectionListener() {

        @Override
        public void onWebSocketOpen() {
        }

        @Override
        public void onConnected() {
        }

        @Override
        public void onDisconnected(final int code, final String reason, final boolean remote) {
        }

});
```
###### Subscription events
```java
centrifugo.setSubscriptionListener(new SubscriptionListener() {

        @Override
        public void onSubscribed(final String channelName) {
        }

        @Override
        public void onUnsubscribed(final String channelName) {
        }

        @Override
        public void onSubscriptionError(final String channelName, final String error) {
        }

});
```
###### Messages, published into channel
```java
centrifugo.setDataMessageListener(new DataMessageListener() {

        @Override
        public void onNewDataMessage(final DataMessage message) {
        }

});
```
##### Join and leave events
```java
centrifugo.setJoinLeaveListener(new JoinLeaveListener() {

        @Override
        public void onJoin(final JoinMessage joinMessage) {
            message(joinMessage.getUser(), " just joined " + joinMessage.getChannel());
        }

        @Override
        public void onLeave(final LeftMessage leftMessage) {
            message(leftMessage.getUser(), " just left " + leftMessage.getChannel());
        }

});
```
##### Request information
You can request history of the channel
```java
centrifugo.requestHistory("my-channel")
```

### Installation
Add

```
compile 'com.github.centrifugal:centrifuge-android:0.36'
```
to <b>dependencies</b> in your <b>build.gradle</b>    

so your build.gradle looks something like this:
```
apply plugin: 'com.android.application'

repositories {
    mavenCentral()
    maven { url "https://oss.sonatype.org/content/repositories/releases/" }
}

...

dependencies {
    compile fileTree(include: ['*.jar'], dir: 'libs')
    testCompile 'junit:junit:4.12'
    compile 'com.github.centrifugal:centrifuge-android:0.36'
}

```

Have a look at example [application](https://github.com/Centrifugal/centrifuge-android/tree/dev/app)
    
    
