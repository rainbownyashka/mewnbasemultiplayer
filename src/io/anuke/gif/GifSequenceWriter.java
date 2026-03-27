/*
 * Decompiled with CFR 0.152.
 */
package io.anuke.gif;

import java.awt.image.RenderedImage;
import java.io.IOException;
import java.util.Iterator;
import javax.imageio.IIOException;
import javax.imageio.IIOImage;
import javax.imageio.ImageIO;
import javax.imageio.ImageTypeSpecifier;
import javax.imageio.ImageWriteParam;
import javax.imageio.ImageWriter;
import javax.imageio.metadata.IIOMetadata;
import javax.imageio.metadata.IIOMetadataNode;
import javax.imageio.stream.ImageOutputStream;

public class GifSequenceWriter {
    protected ImageWriter gifWriter = GifSequenceWriter.getWriter();
    protected ImageWriteParam imageWriteParam = this.gifWriter.getDefaultWriteParam();
    protected IIOMetadata imageMetaData;

    public GifSequenceWriter(ImageOutputStream outputStream, int imageType, int timeBetweenFramesMS, boolean loopContinuously) throws IIOException, IOException {
        ImageTypeSpecifier imageTypeSpecifier = ImageTypeSpecifier.createFromBufferedImageType(imageType);
        this.imageMetaData = this.gifWriter.getDefaultImageMetadata(imageTypeSpecifier, this.imageWriteParam);
        String metaFormatName = this.imageMetaData.getNativeMetadataFormatName();
        IIOMetadataNode root = (IIOMetadataNode)this.imageMetaData.getAsTree(metaFormatName);
        IIOMetadataNode graphicsControlExtensionNode = GifSequenceWriter.getNode(root, "GraphicControlExtension");
        graphicsControlExtensionNode.setAttribute("disposalMethod", "none");
        graphicsControlExtensionNode.setAttribute("userInputFlag", "FALSE");
        graphicsControlExtensionNode.setAttribute("transparentColorFlag", "FALSE");
        graphicsControlExtensionNode.setAttribute("delayTime", Integer.toString(timeBetweenFramesMS / 10));
        graphicsControlExtensionNode.setAttribute("transparentColorIndex", "0");
        IIOMetadataNode commentsNode = GifSequenceWriter.getNode(root, "CommentExtensions");
        commentsNode.setAttribute("CommentExtension", "Created by MAH");
        IIOMetadataNode appEntensionsNode = GifSequenceWriter.getNode(root, "ApplicationExtensions");
        IIOMetadataNode child = new IIOMetadataNode("ApplicationExtension");
        child.setAttribute("applicationID", "NETSCAPE");
        child.setAttribute("authenticationCode", "2.0");
        int loop = loopContinuously ? 0 : 1;
        child.setUserObject(new byte[]{1, (byte)(loop & 0xFF), (byte)(loop >> 8 & 0xFF)});
        appEntensionsNode.appendChild(child);
        this.imageMetaData.setFromTree(metaFormatName, root);
        this.gifWriter.setOutput(outputStream);
        this.gifWriter.prepareWriteSequence(null);
    }

    public void writeToSequence(RenderedImage img) throws IOException {
        this.gifWriter.writeToSequence(new IIOImage(img, null, this.imageMetaData), this.imageWriteParam);
    }

    public void close() throws IOException {
        this.gifWriter.endWriteSequence();
    }

    private static ImageWriter getWriter() throws IIOException {
        Iterator<ImageWriter> iter = ImageIO.getImageWritersBySuffix("gif");
        if (!iter.hasNext()) {
            throw new IIOException("No GIF Image Writers Exist");
        }
        return iter.next();
    }

    private static IIOMetadataNode getNode(IIOMetadataNode rootNode, String nodeName) {
        int nNodes = rootNode.getLength();
        for (int i = 0; i < nNodes; ++i) {
            if (rootNode.item(i).getNodeName().compareToIgnoreCase(nodeName) != 0) continue;
            return (IIOMetadataNode)rootNode.item(i);
        }
        IIOMetadataNode node = new IIOMetadataNode(nodeName);
        rootNode.appendChild(node);
        return node;
    }
}

