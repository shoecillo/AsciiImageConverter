package com.sh.converter;

import java.awt.Graphics2D;
import java.awt.Image;
import java.awt.image.BufferedImage;
import java.io.ByteArrayOutputStream;
import java.io.File;
import java.io.IOException;
import java.io.InputStream;
import java.io.StringWriter;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.nio.file.StandardOpenOption;
import java.util.Base64;
import java.util.Observable;
import java.util.Observer;

import javax.imageio.ImageIO;

public class AsciiImageConverter extends Observable implements Runnable {

	private String ruta;
	
	public static void main(String[] args) {
		
		try {
			String ruta = "H:\\DESKTOP\\Masha Ukr\\Masha01.png";
			new AsciiImageConverter(ruta).convert();
			
		} catch (IOException e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		}
		
	}
	
	public AsciiImageConverter(String ruta) {
		
		this.ruta = ruta;
	}

	public AsciiImageConverter(String ruta,Observer obs) {
		
		this.ruta = ruta;
		this.addObserver(obs);
	}



	@Override
	public void run() {
		
		try {
			convert();
		} catch (IOException e) {
			
			e.printStackTrace();
		}
		
	}
	
	public void convert() throws IOException {
		
		Path pathImg = Paths.get(ruta);
		if(!pathImg.toFile().exists()) {
			throw new IOException("File not exists");
		}
		String[] splitted = pathImg.toFile().getName().split("\\.");
		
		String title  = splitted[0];
		InputStream istr = null;
		
		ByteArrayOutputStream byteOstr = null;
		
		try {
			
			istr = Files.newInputStream(pathImg, StandardOpenOption.READ);
			BufferedImage img = ImageIO.read(istr);

			int h = img.getHeight();
			int w = img.getWidth();
			
			
			// Resize image
			int fixedW = 250;
			int fixedH = (h*fixedW)/w;
			int totalOccurs = fixedH*fixedW;
			int contaOccurs = 0;
			Image tmp = img.getScaledInstance(fixedW, fixedH, Image.SCALE_SMOOTH);
			BufferedImage resized = new BufferedImage(fixedW, fixedH, img.getType());
			Graphics2D g2d = resized.createGraphics();
			g2d.drawImage(tmp, 0, 0, null);
			g2d.dispose();
			
			// covert resized image to base64
			byteOstr = new ByteArrayOutputStream();
			ImageIO.write(resized, "png", byteOstr);
			String b64Img = new String(Base64.getEncoder().encode(byteOstr.toByteArray()),"UTF-8");		
			
			// pixel by pixel
			StringBuffer buff = new StringBuffer();
			for(int y=0;y<fixedH;y++) {
				for(int x=0;x<fixedW;x++) {
					int p = resized.getRGB(x, y);
					// extract the values or alpha,red,green and blue
					int a = (p>>24)&0xff;
					int r = (p>>16)&0xff;
					int g = (p>>8)&0xff;
					int b = (p)&0xff;
					
					// calculate the value in 0-255 range
					int pixel =(a+r+g+b)/4;

					if(pixel<64) {
						buff.append("##");
						
					} else if(pixel<128) {
						buff.append("**");
						
					}else if(pixel<196) {
						buff.append("..");
						
					}else {
						buff.append("  ");
						
					}
					contaOccurs++;
					Integer counter = (contaOccurs*100)/totalOccurs;
					setChanged();
					notifyObservers(counter);
					
				}
				buff.append("\r\n");
				
			}
			
			
			Path p = Paths.get(PathsEnum.GENPATH.getValue()+File.separator+title+"-Img.html");
			if(p.toFile().exists()) {
				Files.delete(p);
			}
			String content = createHtml(title, buff.toString());
			content = content.replaceAll("##B64##", b64Img);
			Files.write(p, content.getBytes(), StandardOpenOption.CREATE);
			
			setChanged();
			notifyObservers(new Integer(-1));
			
		} catch (IOException e) {
			e.printStackTrace();
			
		} finally {
			if(istr !=null) {istr.close();}
			if(byteOstr != null) {byteOstr.close();}
		}
		
	}
	
	private String createHtml(String title,String content) throws IOException {
		
		StringWriter strW = new StringWriter();
		InputStream istrTemplate = null;
		try {
			istrTemplate = AsciiImageConverter.class.getResourceAsStream("/template.html");
			int b = 0;
			byte[] buffer = new byte[4096];
			while ((b = istrTemplate.read(buffer)) > 0) {
				strW.write(new String(buffer,0,b));
			}
			String res = strW.toString().replaceAll("##TITLE##", title);
			res = res.replaceAll("##BODY##", content);
			return res;
		} finally {
			if(istrTemplate != null)
				istrTemplate.close();
		}
	}

	
	
}
