package br.furb;

import java.util.ArrayList;

public class VideoRGBV {

	
	public int red = 0;
	public int green = 0;
	public int blue = 0;
	
	public float variacao = 0;
	
	private int qtdCorte 	= 10;
	
	public VideoRGBV(int r, int g, int b, int qtdCorte){
		this.qtdCorte = qtdCorte;
		this.red = r;
		this.green = g;
		this.blue = b;
	}
	
	public VideoRGBV(ArrayList<int[]> rgbList, int qtdCorte){
		this.qtdCorte = qtdCorte;
		
		int count = rgbList.size();
		int slice = (int)(count / qtdCorte);
		int[] rgb = null;
		for(int i = 1; i < qtdCorte-1; i++){
			
			if(rgb!=null)
				this.variacao += calculaDiferenca(rgb, rgbList.get(slice*i));
			rgb = rgbList.get(slice*i);
			this.red += rgb[0];
			this.green += rgb[1];
			this.blue += rgb[2];
			
		}
		this.red /= qtdCorte;
		this.green /= qtdCorte;
		this.blue /= qtdCorte;
		this.variacao /= qtdCorte-1;
		
	}
	
	private float calculaDiferenca(int[] rgbOld, int[] rgbNew){
		try {
			float r = (rgbNew[0] - rgbOld[0]) / rgbOld[0] * 100;
			float g = (rgbNew[1] - rgbOld[1]) / rgbOld[1] * 100;
			float b = (rgbNew[2] - rgbOld[2]) / rgbOld[2] * 100;
			
			float rgb = (r+g+b)/3;
			
			return rgb > 0 ? rgb : (rgb * -1);
		}catch (ArithmeticException e){
			return 0;
		}
	}
}
