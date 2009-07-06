package com.google.code.osmandroidconverter.polygons;

public class Line {
	protected double a; 
	protected double b;
	protected double c;
	
	private static double smallValue = 0.00001;
	private static double bigValue   = 99999;
	
	public Line(double angleInRad, Point point)
	{
		Initialize(angleInRad, point);
	}
	
	private void Initialize(Double angleInRad, Point point)
	{
		//angleInRad should be between 0-Pi
		
		try
		{
			//if ((angleInRad<0) ||(angleInRad>Math.PI))
			if (angleInRad>2*Math.PI)
			{
				throw new Exception();
			}
		
			if (Math.abs(angleInRad-Math.PI/2)<smallValue) //vertical line
			{
				a=1;
				b=0;
				c=-point.getX();
			}
			else //not vertical line
			{				
				a=-Math.tan(angleInRad);
				b=1;
				c=-a*point.getX()-b*point.getY();
			}
		}
		catch (Exception e)
		{
			;
		}
	}
	
	public Line(Point point1, Point point2)
	{			
		try
		{
			if (Point.SamePoints(point1, point2))
			{
				String errMsg="The input points are the same";
				throw new Exception();	
			}			

			//Point1 and Point2 are different points:
			if (Math.abs(point1.getX()-point2.getX())<smallValue) //vertical line
			{
				Initialize(Math.PI/2, point1);
			}
			else if (Math.abs(point1.getY()-point2.getY())<smallValue) //Horizontal line
			{
				Initialize((double)0, point1);
			}
			else //normal line
			{
				double m=(point2.getY()-point1.getY())/(point2.getX()-point1.getX());
				double alphaInRad=Math.atan(m);
				Initialize(alphaInRad, point1);
			}
		}
		catch (Exception e)
		{
			;
		}
	}
	
	public Line(Line copiedLine)
	{
		this.a=copiedLine.a; 
		this.b=copiedLine.b;
		this.c=copiedLine.c;
	}

	/*** calculate the distance from a given point to the line ***/ 
	public double GetDistance(Point point)
	{
		double x0=point.getX();
		double y0=point.getY();

		double d=Math.abs(a*x0+b*y0+c);
		d=d/(Math.sqrt(a*a+b*b));
		
		return d;			
	}

	/*** point(x, y) in the line, based on y, calculate x ***/ 
	public double GetX(double y)
	{
		//if the line is a horizontal line (a=0), it will return a NaN:
		double x;
		try
		{
			if (Math.abs(a)<smallValue) //a=0;
			{
				throw new Exception();
			}
			
			x=-(b*y+c)/a;
		}
		catch (Exception e)  //Horizontal line a=0;
		{
			x=Double.NaN;
		}
			
		return x;
	}

	/*** point(x, y) in the line, based on x, calculate y ***/ 
	public double GetY(double x)
	{
		//if the line is a vertical line, it will return a NaN:
		double y;
		try
		{
			if (Math.abs(b)<smallValue)
			{
				throw new Exception();
			}
			y=-(a*x+c)/b;
		}
		catch (Exception e)
		{
			y=Double.NaN;
		
		}
		return y;
	}

	/*** is it a vertical line:***/
	public boolean VerticalLine()
	{
		if (Math.abs(b-0)<smallValue)
			return true;
		else
			return false;
	}

	/*** is it a horizontal line:***/
	public boolean HorizontalLine()
	{
		if (Math.abs(a-0)<smallValue)
			return true;
		else
			return false;
	}

	/*** calculate line angle in radian: ***/
	public double GetLineAngle()
	{
		if (b==0)
		{
			return Math.PI/2;
		}
		else //b!=0
		{
			double tanA=-a/b;
			return Math.atan(tanA);
		}			
	}

	public boolean Parallel(Line line)
	{
		boolean bParallel=false;
		if (this.a/this.b==line.a/line.b)
			bParallel=true;

		return bParallel;
	}

	/**************************************
	 Calculate intersection point of two lines
	 if two lines are parallel, return null
	 * ************************************/
	public Point IntersecctionWith(Line line)
	{
		Point point=new Point();
		double a1=this.a;
		double b1=this.b;
		double c1=this.c;

		double a2=line.a;
		double b2=line.b;
		double c2=line.c;

		if (!(this.Parallel(line))) //not parallen
		{
			point.setX((c2*b1-c1*b2)/(a1*b2-a2*b1));
			point.setY((a1*c2-c1*a2)/(a2*b2-a1*b2));
		}
		return point;
		}
}




