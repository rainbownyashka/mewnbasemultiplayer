package com.cairn4.moonbase.ui;

import com.badlogic.gdx.Gdx;
import com.badlogic.gdx.graphics.Color;
import com.badlogic.gdx.graphics.g2d.BitmapFont;
import com.badlogic.gdx.graphics.g2d.NinePatch;
import com.badlogic.gdx.scenes.scene2d.InputEvent;
import com.badlogic.gdx.scenes.scene2d.ui.Label;
import com.badlogic.gdx.scenes.scene2d.ui.ScrollPane;
import com.badlogic.gdx.scenes.scene2d.ui.Table;
import com.badlogic.gdx.scenes.scene2d.ui.TextButton;
import com.badlogic.gdx.scenes.scene2d.utils.ClickListener;
import com.badlogic.gdx.scenes.scene2d.utils.NinePatchDrawable;
import com.cairn4.moonbase.MpServerRegistry;
import java.util.List;

public class MultiplayerServerListMenu extends Menu {
    private final MultiplayerConfigMenu parent;
    private Label statusLabel;
    private Table listTable;

    public MultiplayerServerListMenu(BaseScreen baseScreen, MultiplayerConfigMenu parent) {
        super(baseScreen);
        this.parent = parent;
        this.setup();
        this.refreshList();
    }

    @Override
    protected void setup() {
        this.menuGroup.setTransform(false);
        this.menuGroup.setSize(this.stage.getWidth(), this.stage.getHeight());
        Table root = new Table();
        root.setFillParent(true);
        this.menuGroup.addActor(root);

        NinePatchDrawable alt_npd_up = new NinePatchDrawable(new NinePatch(this.skin.getRegion("btn-minor"), 16, 16, 16, 16));
        NinePatchDrawable alt_npd_active = new NinePatchDrawable(new NinePatch(this.skin.getRegion("btn-minor-pressed"), 16, 16, 16, 16));
        TextButton.TextButtonStyle buttonStyle = new TextButton.TextButtonStyle();
        buttonStyle.font = new BitmapFont();
        buttonStyle.fontColor = Color.WHITE;
        buttonStyle.up = alt_npd_up;
        buttonStyle.down = alt_npd_active;
        Label.LabelStyle labelStyle = new Label.LabelStyle(new BitmapFont(), Color.WHITE);

        root.add(new Label("Online servers (mirror)", labelStyle)).pad(10.0f);
        root.row();
        this.statusLabel = new Label("Loading...", labelStyle);
        root.add(this.statusLabel).pad(6.0f);
        root.row();

        this.listTable = new Table();
        ScrollPane scroll = new ScrollPane(this.listTable);
        scroll.setFadeScrollBars(false);
        root.add(scroll).width(420.0f).height(220.0f).pad(8.0f);
        root.row();

        TextButton refreshButton = new TextButton("Refresh", buttonStyle);
        refreshButton.addListener(new ClickListener() {
            @Override
            public void clicked(InputEvent event, float x, float y) {
                MultiplayerServerListMenu.this.baseScreen.menuForwardSound();
                MultiplayerServerListMenu.this.refreshList();
            }
        });
        TextButton backButton = new TextButton("Back", buttonStyle);
        backButton.addListener(new ClickListener() {
            @Override
            public void clicked(InputEvent event, float x, float y) {
                MultiplayerServerListMenu.this.baseScreen.menuBackSound();
                MultiplayerServerListMenu.this.back();
            }
        });
        root.add(refreshButton).pad(12.0f);
        root.add(backButton).pad(12.0f);
    }

    private void refreshList() {
        if (this.statusLabel != null) {
            this.statusLabel.setText("Loading...");
        }
        if (this.listTable != null) {
            this.listTable.clear();
        }
        MpServerRegistry.fetchServerList((entries, error) -> {
            if (MultiplayerServerListMenu.this.listTable == null) {
                return;
            }
            MultiplayerServerListMenu.this.listTable.clear();
            if (error != null && error.length() > 0 && (entries == null || entries.isEmpty())) {
                MultiplayerServerListMenu.this.statusLabel.setText(error);
                return;
            }
            if (entries == null || entries.isEmpty()) {
                MultiplayerServerListMenu.this.statusLabel.setText("No servers online");
                return;
            }
            MultiplayerServerListMenu.this.statusLabel.setText(entries.size() + " server(s)");
            NinePatchDrawable alt_npd_up = new NinePatchDrawable(new NinePatch(MultiplayerServerListMenu.this.skin.getRegion("btn-minor"), 16, 16, 16, 16));
            NinePatchDrawable alt_npd_active = new NinePatchDrawable(new NinePatch(MultiplayerServerListMenu.this.skin.getRegion("btn-minor-pressed"), 16, 16, 16, 16));
            TextButton.TextButtonStyle buttonStyle = new TextButton.TextButtonStyle();
            buttonStyle.font = new BitmapFont();
            buttonStyle.fontColor = Color.WHITE;
            buttonStyle.up = alt_npd_up;
            buttonStyle.down = alt_npd_active;
            for (final MpServerRegistry.Entry e : entries) {
                String label = (e.name.length() > 0 ? e.name : e.host) + "  (" + e.players + ")  " + e.host + ":" + e.port;
                TextButton row = new TextButton(label, buttonStyle);
                row.addListener(new ClickListener() {
                    @Override
                    public void clicked(InputEvent event, float x, float y) {
                        MultiplayerServerListMenu.this.baseScreen.menuForwardSound();
                        MultiplayerServerListMenu.this.parent.applyServer(e.host, e.port);
                        MultiplayerServerListMenu.this.back();
                    }
                });
                MultiplayerServerListMenu.this.listTable.add(row).fillX().pad(4.0f);
                MultiplayerServerListMenu.this.listTable.row();
            }
        });
    }
}
