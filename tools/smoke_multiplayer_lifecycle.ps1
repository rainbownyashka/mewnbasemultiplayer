param([int]$HostEvalPort = 8793, [int]$GuestEvalPort = 8794, [int]$GamePort = 18777, [int]$Cycles = 10)
$ErrorActionPreference = 'Stop'
# Run ONLY against two disposable game copies with a copied saves/tesm.
$evalScript = Join-Path $PSScriptRoot 'runtime_eval.ps1'
function Invoke-Game([int]$Port, [string]$Body) {
    $Body = $Body -replace '\bgs\b', 'testGs'
    $code = 'try { final com.cairn4.moonbase.ui.GameScreen testGs=gs; java.util.concurrent.FutureTask<String> task=new java.util.concurrent.FutureTask<String>(() -> {' + $Body + '}); com.badlogic.gdx.Gdx.app.postRunnable(task); return task.get(25,java.util.concurrent.TimeUnit.SECONDS); } catch(Throwable e) { java.io.StringWriter sw=new java.io.StringWriter(); e.printStackTrace(new java.io.PrintWriter(sw)); return "FAIL "+sw.toString(); }'
    $result = & $evalScript -Port $Port -Code $code
    if ($result -match 'ERROR|ERR:|Exception|FAIL') { throw $result }
    return $result.Trim()
}
function Wait-Game([int]$Port, [string]$Body, [string]$Expected) {
    $deadline = [DateTime]::UtcNow.AddSeconds(35)
    do {
        $result = Invoke-Game $Port $Body
        if ($result -eq $Expected) { return }
        Start-Sleep -Milliseconds 250
    } while ([DateTime]::UtcNow -lt $deadline)
    throw "Timeout: expected $Expected, got $result on $Port"
}
function Assert-Game([int]$Port, [string]$Body, [string]$Expected) {
    $result = Invoke-Game $Port $Body
    if ($result -ne $Expected) { throw "Expected $Expected, got $result on $Port" }
}
for ($cycle=1; $cycle -le $Cycles; $cycle++) {
    if ((Invoke-Game $HostEvalPort 'return com.cairn4.moonbase.Server.getActiveServer()==null ? "missing" : "present";') -eq 'missing') {
        Invoke-Game $HostEvalPort ('com.cairn4.moonbase.MoonBase.pendingHostServerOptions=new com.cairn4.moonbase.MoonBase.PendingHostServerOptions(); com.cairn4.moonbase.ui.HostServerSaveMenu m=new com.cairn4.moonbase.ui.HostServerSaveMenu((com.cairn4.moonbase.ui.BaseScreen)ctx.getScreen(),' + $GamePort + '); java.lang.reflect.Method load=m.getClass().getDeclaredMethod("loadGame",String.class); load.setAccessible(true); load.invoke(m,"tesm"); return "host queued";') | Out-Null
    }
    Wait-Game $HostEvalPort 'com.cairn4.moonbase.Server s=com.cairn4.moonbase.Server.getActiveServer(); return s!=null && s.isListening() ? "ready" : "waiting";' 'ready'
    Invoke-Game $GuestEvalPort ('if(testGs!=null && testGs.isSessionActive()) return "already connected"; com.cairn4.moonbase.AssetManagerSingleton.getInstance().load("loading.atlas",com.badlogic.gdx.graphics.g2d.TextureAtlas.class); com.cairn4.moonbase.AssetManagerSingleton.getInstance().load("loading-bg.png",com.badlogic.gdx.graphics.Texture.class); com.cairn4.moonbase.AssetManagerSingleton.getInstance().finishLoading(); com.cairn4.moonbase.MoonBase.GOD_MODE=true; com.cairn4.moonbase.MoonBase.isMultiplayer=true; com.cairn4.moonbase.MoonBase.multiplayerHost="127.0.0.1"; com.cairn4.moonbase.MoonBase.multiplayerPort=' + $GamePort + '; com.cairn4.moonbase.MoonBase.multiplayerNick="LifecycleGuest"; com.cairn4.moonbase.MoonBase.multiplayerPassword=""; com.cairn4.moonbase.MoonBase.currentSaveFolder="multiplayer_received"; ctx.setScreen(new com.cairn4.moonbase.ui.LoadingScreen(ctx,false)); return "guest queued";') | Out-Null
    Wait-Game $GuestEvalPort 'return gs!=null && gs.world.player!=null && com.cairn4.moonbase.MoonBase.multiplayerSyncReady && gs.world.gameState==com.cairn4.moonbase.World.GameStates.playing ? "ready" : "waiting";' 'ready'
    Wait-Game $HostEvalPort 'return gs!=null && gs.getRemotePlayer(1)!=null ? "ready" : "waiting";' 'ready'
    foreach ($port in @($HostEvalPort,$GuestEvalPort)) {
        Assert-Game $port 'for(String cause:new String[]{"air","heat"}) { com.cairn4.moonbase.PlayerStatus st=gs.world.player.playerStatus; st.resetForMultiplayerRespawn(); java.lang.reflect.Field habitat=st.getClass().getDeclaredField("currentHabitatModule"); habitat.setAccessible(true); habitat.set(st,null); java.lang.reflect.Field temp=st.getClass().getDeclaredField("temperature"); temp.setAccessible(true); temp.setFloat(st,"heat".equals(cause)?10:0); java.lang.reflect.Field timer=st.getClass().getDeclaredField("tempUpdateTimer"); timer.setAccessible(true); timer.setFloat(st,0); if("air".equals(cause)) st.setAir(0); st.setHealth(0.001f); st.update(0.01f); if(st.getHealth()<99 || gs.world.gameState!=com.cairn4.moonbase.World.GameStates.playing) return "FAIL death cause="+cause; } return "OK";' 'OK'
        Assert-Game $port 'int before=gs.hudStage.getActors().size; gs.showMenu(new com.cairn4.moonbase.ui.MiniMap(gs)); gs.world.player.playerStatus.setHealth(0); gs.world.player.playerStatus.update(0); return gs.hudStage.getActors().size==before && gs.world.gameState==com.cairn4.moonbase.World.GameStates.playing ? "OK" : "FAIL death while map open";' 'OK'
        Assert-Game $port 'int before=gs.hudStage.getActors().size; com.cairn4.moonbase.ui.MiniMap m=new com.cairn4.moonbase.ui.MiniMap(gs); gs.showMenu(m); if(gs.world.gameState!=com.cairn4.moonbase.World.GameStates.playing) return "FAIL map paused multiplayer"; m.back(); return gs.hudStage.getActors().size==before ? "OK" : "FAIL map actor leak";' 'OK'
        Assert-Game $port 'gs.world.player.openMap(); gs.world.player.playerStatus.setHealth(0); gs.world.player.playerStatus.update(0); if(gs.world.player.playerStatus.getHealth()<=0) return "FAIL health"; java.lang.reflect.Field f=gs.world.player.getClass().getDeclaredField("pendingMapOpen"); f.setAccessible(true); if(f.get(gs.world.player)!=null) return "FAIL delayed map"; return gs.world.gameState==com.cairn4.moonbase.World.GameStates.playing && !"death".equals(gs.world.player.spineActor.state.getCurrent(0).getAnimation().getName()) ? "OK" : "FAIL respawn";' 'OK'
    }
    Assert-Game $HostEvalPort 'return com.cairn4.moonbase.Server.getActiveServer().getClientCount()==1 ? "OK" : "FAIL disconnected during respawn";' 'OK'
    Invoke-Game $HostEvalPort 'final com.cairn4.moonbase.ui.GameScreen old=gs; old.postSessionRunnable(() -> {throw new AssertionError("stale session callback ran");}); gs.dispose(); gs.dispose(); old.removePlayer(1); com.badlogic.gdx.Gdx.app.postRunnable(() -> old.removePlayer(1)); ctx.loadFrontendAssets(); com.cairn4.moonbase.AssetManagerSingleton.getInstance().finishLoading(); ctx.setScreen(new com.cairn4.moonbase.ui.FrontendScreen(ctx)); return "closed";' | Out-Null
    Wait-Game $GuestEvalPort 'return ctx.getScreen() instanceof com.cairn4.moonbase.ui.FrontendScreen ? "closed" : "waiting";' 'closed'
    Wait-Game $HostEvalPort 'int n=0; for(Thread t:Thread.getAllStackTraces().keySet()) if(t.isAlive() && (t.getName().equals("MewnBase-Server-HostPos") || t.getName().startsWith("MewnBase-Client-Writer-") || t.getName().startsWith("MewnBase-Client-Resend-"))) n++; return n==0 ? "stopped" : "threads="+n;' 'stopped'
    Write-Output "PASS cycle=$cycle host+guest respawn, map cleanup, late disconnect, repeat dispose, thread cleanup"
}
