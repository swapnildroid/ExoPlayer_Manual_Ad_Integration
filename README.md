# README

## Contents
1. [Screenshot][#screenshot]
2. [HLD][#HLD]
3. [LLD Component Class Diagram][#LLD — Component Class Diagram]
4. [LLD Playback Control Flow][#LLD — Playback Control Flow]

### Screenshot
![Screenshot_20260115_200257.png](Screenshot_20260115_200257.png)

### HLD
```mermaid
flowchart TD

UI[Jetpack Compose UI]
UI --> AV[AndroidView PlayerView]

AV --> Surface[Video Surface]

Surface --> MP[Main ExoPlayer]
Surface --> AP[Ad ExoPlayer]

MP --> MC[Main Content Video]
AP --> AD[Ad Video]

MP --> MS[Main Playback State]
AP --> AS[Ad Playback State]

MS --> CS[Compose State Main]
AS --> CS2[Compose State Ad]

CS --> AE[Ad Decision Engine]
AE -->|Time threshold reached| ShowAd[showAd true]
ShowAd -->|Pause main| MP
ShowAd -->|Play ad| AP

AS -->|Ad ended| Resume[Resume Main]
Resume -->|Pause ad| AP
Resume -->|Play main| MP
Resume -->|showAd false| UI

CS2 --> Switch[Player Switch Logic]
Switch --> AV

Buttons[User Skip Buttons] --> MP
Buttons --> AE
```

### LLD — Component Class Diagram 
```mermaid
classDiagram

class MainActivity {
    +onCreate()
}

class Greeting {
    +PlayerScreen()
}

class PlayerScreen {
    -mainPlayer : ExoPlayer
    -adPlayer : ExoPlayer
    -playerView : PlayerView
    -showAd : Boolean
    -isMainPlaying : Boolean
    -isAdPlaying : Boolean
    -mainPlaybackState : Int
    -adPlaybackState : Int
    -adIntervalCounter : Int
}

class ExoPlayer {
    +play()
    +pause()
    +seekTo()
    +prepare()
    +release()
    +addListener()
}

class PlayerView {
    +player : ExoPlayer
}

class AdDecisionEngine {
    +evaluatePlayback()
    +triggerAd()
}

class PlayerListener {
    +onPlaybackStateChanged()
    +onIsPlayingChanged()
}

MainActivity --> Greeting
Greeting --> PlayerScreen
PlayerScreen --> PlayerView
PlayerScreen --> ExoPlayer : mainPlayer
PlayerScreen --> ExoPlayer : adPlayer
PlayerScreen --> AdDecisionEngine
ExoPlayer --> PlayerListener
PlayerView --> ExoPlayer
```

### LLD — Playback Control Flow
```mermaid
sequenceDiagram

participant UI as Compose UI
participant PV as PlayerView
participant MP as Main ExoPlayer
participant AP as Ad ExoPlayer
participant AE as Ad Engine

UI->>PV: Attach PlayerView
UI->>MP: Prepare main video
UI->>MP: Play

loop Every 500ms
    MP->>AE: currentPosition
    AE->>AE: check time threshold
    AE-->>MP: pause()
    AE-->>AP: play()
    AE-->>UI: showAd = true
    UI-->>PV: switch player to AP
end

AP->>AE: STATE_ENDED
AE-->>AP: pause + seekTo(0)
AE-->>MP: play
AE-->>UI: showAd = false
UI-->>PV: switch player to MP

```
