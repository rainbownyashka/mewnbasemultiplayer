/*
 * Decompiled with CFR 0.152.
 */
package com.badlogic.gdx.tools.texturepacker;

import com.badlogic.gdx.tools.texturepacker.TexturePacker;
import com.badlogic.gdx.utils.Array;

public class GridPacker
implements TexturePacker.Packer {
    private final TexturePacker.Settings settings;

    public GridPacker(TexturePacker.Settings settings) {
        this.settings = settings;
    }

    @Override
    public Array<TexturePacker.Page> pack(Array<TexturePacker.Rect> inputRects) {
        return this.pack(null, inputRects);
    }

    @Override
    public Array<TexturePacker.Page> pack(TexturePacker.ProgressListener progress, Array<TexturePacker.Rect> inputRects) {
        if (!this.settings.silent) {
            System.out.print("Packing");
        }
        int paddingX = this.settings.paddingX;
        int paddingY = this.settings.paddingY;
        int adjustX = paddingX;
        int adjustY = paddingY;
        if (this.settings.edgePadding) {
            if (this.settings.duplicatePadding) {
                adjustX -= paddingX;
                adjustY -= paddingY;
            } else {
                adjustX -= paddingX * 2;
                adjustY -= paddingY * 2;
            }
        }
        int maxWidth = this.settings.maxWidth + adjustX;
        int maxHeight = this.settings.maxHeight + adjustY;
        int n = inputRects.size;
        int cellWidth = 0;
        int cellHeight = 0;
        for (int i = 0; i < n; ++i) {
            TexturePacker.Rect rect = inputRects.get(i);
            cellWidth = Math.max(cellWidth, rect.width);
            cellHeight = Math.max(cellHeight, rect.height);
        }
        cellWidth += paddingX;
        cellHeight += paddingY;
        inputRects.reverse();
        Array<TexturePacker.Page> pages = new Array<TexturePacker.Page>();
        while (inputRects.size > 0) {
            progress.count = n - inputRects.size + 1;
            if (progress.update(progress.count, n)) break;
            TexturePacker.Page page = this.packPage(inputRects, cellWidth, cellHeight, maxWidth, maxHeight);
            page.width -= paddingX;
            page.height -= paddingY;
            pages.add(page);
        }
        return pages;
    }

    private TexturePacker.Page packPage(Array<TexturePacker.Rect> inputRects, int cellWidth, int cellHeight, int maxWidth, int maxHeight) {
        TexturePacker.Rect rect;
        int i;
        TexturePacker.Page page = new TexturePacker.Page();
        page.outputRects = new Array();
        int n = inputRects.size;
        int x = 0;
        int y = 0;
        for (i = n - 1; i >= 0; --i) {
            if (x + cellWidth > maxWidth) {
                if ((y += cellHeight) > maxHeight - cellHeight) break;
                x = 0;
            }
            rect = inputRects.removeIndex(i);
            rect.x = x;
            rect.y = y;
            rect.width += this.settings.paddingX;
            rect.height += this.settings.paddingY;
            page.outputRects.add(rect);
            page.width = Math.max(page.width, x += cellWidth);
            page.height = Math.max(page.height, y + cellHeight);
        }
        for (i = page.outputRects.size - 1; i >= 0; --i) {
            rect = page.outputRects.get(i);
            rect.y = page.height - rect.y - rect.height;
        }
        return page;
    }
}

