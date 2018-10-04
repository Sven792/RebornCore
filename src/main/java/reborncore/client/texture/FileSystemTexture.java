/*
 * Copyright (c) 2018 modmuss50 and Gigabit101
 *
 *
 *
 * Permission is hereby granted, free of charge, to any person obtaining a copy
 * of this software and associated documentation files (the "Software"), to deal
 * in the Software without restriction, including without limitation the rights
 * to use, copy, modify, merge, publish, distribute, sublicense, and/or sell
 * copies of the Software, and to permit persons to whom the Software is
 * furnished to do so, subject to the following conditions:
 *
 *
 *
 * The above copyright notice and this permission notice shall be included in
 * all copies or substantial portions of the Software.
 *
 *
 *
 * THE SOFTWARE IS PROVIDED "AS IS", WITHOUT WARRANTY OF ANY KIND, EXPRESS OR
 * IMPLIED, INCLUDING BUT NOT LIMITED TO THE WARRANTIES OF MERCHANTABILITY,
 * FITNESS FOR A PARTICULAR PURPOSE AND NONINFRINGEMENT.  IN NO EVENT SHALL THE
 * AUTHORS OR COPYRIGHT HOLDERS BE LIABLE FOR ANY CLAIM, DAMAGES OR OTHER
 * LIABILITY, WHETHER IN AN ACTION OF CONTRACT, TORT OR OTHERWISE, ARISING FROM,
 * OUT OF OR IN CONNECTION WITH THE SOFTWARE OR THE USE OR OTHER DEALINGS IN
 * THE SOFTWARE.
 */

package reborncore.client.texture;

import net.minecraft.client.renderer.texture.AbstractTexture;
import net.minecraft.client.renderer.texture.NativeImage;
import net.minecraft.client.renderer.texture.TextureUtil;
import net.minecraft.resources.IResource;
import net.minecraft.resources.IResourceManager;
import net.minecraft.resources.data.IMetadataSectionSerializer;
import net.minecraft.util.ResourceLocation;
import org.apache.commons.io.IOUtils;

import javax.annotation.Nullable;
import java.io.*;

/**
 * Created by Mark on 25/03/2016.
 */
public class FileSystemTexture extends AbstractTexture {
	protected final File textureLocation;
	NativeImage image;

	public FileSystemTexture(File textureResourceLocation) {
		this.textureLocation = textureResourceLocation;
	}

	@Override
	public void loadTexture(IResourceManager resourceManager) throws IOException {
		this.deleteGlTexture();
		if (image == null) {
			IResource iresource = null;
			try {
				iresource = new IResource() {

					FileInputStream stream;

					@Override
					public ResourceLocation getLocation() {
						return new ResourceLocation("reborncore:loaded/" + textureLocation.getName());
					}

					@Override
					public InputStream getInputStream() {
						if (stream == null) {
							try {
								stream = new FileInputStream(textureLocation);
							} catch (FileNotFoundException e) {
								e.printStackTrace();
							}
						}
						return stream;
					}

					@Override
					public boolean hasMetadata() {
						return false;
					}

					@Nullable
					@Override
					public <T> T getMetadata(IMetadataSectionSerializer<T> serializer) {
						return null;
					}

					@Override
					public String getPackName() {
						return "reborncore";
					}

					@Override
					public void close() throws IOException {
						if (stream != null) {
							stream.close();
						}
					}
				};
				image = NativeImage.read(iresource.getInputStream());
			} finally {
				IOUtils.closeQuietly(iresource);
			}
		}
		this.bindTexture();
		TextureUtil.allocateTextureImpl(this.getGlTextureId(), 0, image.getWidth(), image.getHeight());
		image.uploadTextureSub(0, 0, 0, 0, 0, image.getWidth(), image.getHeight(), false, false, false);
	}
}
