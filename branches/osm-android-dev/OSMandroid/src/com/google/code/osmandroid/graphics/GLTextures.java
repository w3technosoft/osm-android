package com.google.code.osmandroid.graphics;

import java.io.InputStream;
import java.nio.ByteBuffer;
import java.util.HashMap;

import javax.microedition.khronos.opengles.GL10;

import android.content.Context;
import android.content.res.Resources;
import android.graphics.Bitmap;
import android.graphics.BitmapFactory;
import android.opengl.GLUtils;

public class GLTextures {
	
	private HashMap<Integer, Integer>  textureMap;
	private int[]    textureFiles;
	private int      numTextures;
	private Context  context;
	private int[] 	 textures;
	
	public GLTextures(Context context, int maxTextures) {
		
		this.context 	  = context;
		this.textureMap   = new HashMap<Integer, Integer>();
		this.textureFiles = new int[maxTextures];
		this.numTextures  = 0;
	} 
	
	public void add(int resource) {
		
		int[] files = this.textureFiles;
				
		if (this.numTextures == files.length) {
			throw new RuntimeException("Maximum number of textures reached");
		}
		
		files[this.numTextures++] = resource;
	} 
	
	
	public void loadTextures(GL10 gl) {
		
		int[] files = this.textureFiles;
		int numFiles = this.numTextures;
		
		int[] tmp_tex = new int[numFiles];
		gl.glGenTextures(numFiles, tmp_tex, 0);
	
		this.textures = tmp_tex;
		
		InputStream is;
		Bitmap bitmap;
		Resources resources = this.context.getResources();
		
		for(int i = 0; i < numFiles; i++) {
				
			is = resources.openRawResource(files[i]);
			bitmap = BitmapFactory.decodeStream(is);
		
			this.textureMap.put(new Integer(files[i]),new Integer(i));

			gl.glBindTexture(GL10.GL_TEXTURE_2D, tmp_tex[i]);			
	        gl.glTexParameterf(GL10.GL_TEXTURE_2D, GL10.GL_TEXTURE_MIN_FILTER, GL10.GL_NEAREST);
	        gl.glTexParameterf(GL10.GL_TEXTURE_2D, GL10.GL_TEXTURE_MAG_FILTER, GL10.GL_NEAREST);
	        
	        gl.glTexEnvf(GL10.GL_TEXTURE_ENV, GL10.GL_TEXTURE_ENV_MODE, GL10.GL_REPLACE);
			
			GLUtils.texImage2D(GL10.GL_TEXTURE_2D, 0, bitmap, 0);
		}
	} 
	
	public boolean setTexture(GL10 gl, int id) { 
		
		try {
			int textureid = this.textureMap.get(new Integer(id)).intValue();
			gl.glBindTexture(GL10.GL_TEXTURE_2D, this.textures[textureid]);
		}
		catch(Exception e) {
			return false;
		} 
		
		return true;
	}
}
