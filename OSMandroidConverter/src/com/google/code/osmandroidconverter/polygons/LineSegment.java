package com.google.code.osmandroidconverter.polygons;

public class LineSegment extends Line{

	//line: ax+by+c=0, with start point and end point
	//direction from start point ->end point
	private Point m_startPoint;
	private Point m_endPoint;
	private static  double smallValue=0.00001;
	private static double bigValue=99999;
	
	public Point getStartPoint()
	{
		return m_startPoint;	
	}

	public Point getEndPoint()
	{
		return m_endPoint;
	}

	public LineSegment(Point startPoint, Point endPoint)
	{
		super(startPoint,endPoint);
		this.m_startPoint=startPoint;
		this.m_endPoint= endPoint;
	}

	/*** chagne the line's direction ***/
	public void ChangeLineDirection()
	{
		Point tempPt;
		tempPt=this.m_startPoint;
		this.m_startPoint=this.m_endPoint;
		this.m_endPoint=tempPt;
	}
	
	/*** To calculate the line segment length:   ***/
	public double GetLineSegmentLength()
	{
		double d=(m_endPoint.getX()-m_startPoint.getX())	*(m_endPoint.getX()-m_startPoint.getX());
		d += (m_endPoint.getY()-m_startPoint.getY())	*(m_endPoint.getY()-m_startPoint.getY());
		d=Math.sqrt(d);

		return d;
	}

	public int GetPointLocation(Point point)
	{
		double Ax, Ay, Bx, By, Cx, Cy;
		Bx=m_endPoint.getX();
		By=m_endPoint.getY();
		  
		Ax=m_startPoint.getX();
		Ay=m_startPoint.getY();
		  
		Cx=point.getX();
		Cy=point.getY();
		
		if (this.HorizontalLine())
		{
			if (Math.abs(Ay-Cy)<smallValue) //equal
				return 0;
			else if (Ay > Cy)
				return -1;   //Y Axis points down, point is above the line
			else //Ay<Cy
				return 1;    //Y Axis points down, point is below the line
		}
		else //Not a horizontal line
		{
			//make the line direction bottom->up
			if (m_endPoint.getY()>m_startPoint.getY())
				this.ChangeLineDirection();

			double L=this.GetLineSegmentLength();
			double s=((Ay-Cy)*(Bx-Ax)-(Ax-Cx)*(By-Ay))/(L*L);
			 
			//Note: the Y axis is pointing down:
			if (Math.abs(s-0)<smallValue) //s=0
				return 0; //point is in the line or line extension
			else if (s>0) 
				return -1; //point is left of line or above the horizontal line
			else //s<0
				return 1;
		}
	}

	/***Get the minimum x value of the points in the line***/
	public double GetXmin()
	{
		return Math.min(m_startPoint.getX(), m_endPoint.getX());
	}

	/***Get the maximum  x value of the points in the line***/
	public double GetXmax()
	{
		return Math.max(m_startPoint.getX(), m_endPoint.getX());
	}

	/***Get the minimum y value of the points in the line***/
	public double GetYmin()
	{
		return Math.min(m_startPoint.getY(), m_endPoint.getY());
	}

	/***Get the maximum y value of the points in the line***/
	public double GetYmax()
	{
		return Math.max(m_startPoint.getY(), m_endPoint.getY());
	}

	/***Check whether this line is in a longer line***/
	public boolean InLine(LineSegment longerLineSegment)
	{
		boolean bInLine=false;
		if ((m_startPoint.InLine(longerLineSegment)) &&
			(m_endPoint.InLine(longerLineSegment)))
			bInLine=true;
		return bInLine;
	}

	
	/************************************************
	 * Offset the line segment to generate a new line segment
	 * If the offset direction is along the x-axis or y-axis, 
	 * Parameter is true, other wise it is false
	 * ***********************************************/
	public LineSegment OffsetLine(double distance, boolean rightOrDown)
	{
		//offset a line with a given distance, generate a new line
		//rightOrDown=true means offset to x incress direction,
		// if the line is horizontal, offset to y incress direction

		LineSegment line;
		Point newStartPoint=new Point();
		Point newEndPoint=new Point();
		
		double alphaInRad= this.GetLineAngle(); // 0-PI
		if (rightOrDown)
		{
			if (this.HorizontalLine()) //offset to y+ direction
			{
				newStartPoint.setX(this.m_startPoint.getX());
				newStartPoint.setX(this.m_startPoint.getY() + distance);

				newEndPoint.setX(this.m_endPoint.getX());
				newEndPoint.setY(this.m_endPoint.getY() + distance);
				line=new LineSegment(newStartPoint,newEndPoint);
			}
			else //offset to x+ direction
			{
				if (Math.sin(alphaInRad)>0)  
				{
					newStartPoint.setX(m_startPoint.getX() + Math.abs(distance*Math.sin(alphaInRad)));
					newStartPoint.setY(m_startPoint.getY() - Math.abs(distance* Math.cos(alphaInRad))) ;
					
					newEndPoint.setX(m_endPoint.getX() + Math.abs(distance*Math.sin(alphaInRad)));
					newEndPoint.setY(m_endPoint.getY() - Math.abs(distance* Math.cos(alphaInRad))) ;
				
					line= new LineSegment(newStartPoint, newEndPoint);
				}
				else //sin(FalphaInRad)<0
				{
					newStartPoint.setX(m_startPoint.getX() + Math.abs(distance*Math.sin(alphaInRad)));
					newStartPoint.setY(m_startPoint.getY() + Math.abs(distance* Math.cos(alphaInRad))) ;
					newEndPoint.setX(m_endPoint.getX() + Math.abs(distance*Math.sin(alphaInRad)));
					newEndPoint.setY(m_endPoint.getY() + Math.abs(distance* Math.cos(alphaInRad))) ;

					line=new LineSegment(newStartPoint, newEndPoint);
				}
			} 
		}//{rightOrDown}
		else //leftOrUp
		{
			if (this.HorizontalLine()) //offset to y directin
			{
				newStartPoint.setX(m_startPoint.getX());
				newStartPoint.setY(m_startPoint.getY() - distance);

				newEndPoint.setX(m_endPoint.getX());
				newEndPoint.setY(m_endPoint.getY() - distance);
				line=new LineSegment(newStartPoint, newEndPoint);
			}
			else //offset to x directin
			{
				if (Math.sin(alphaInRad)>=0)
				{
					newStartPoint.setX(m_startPoint.getX() - Math.abs(distance*Math.sin(alphaInRad)));
					newStartPoint.setY(m_startPoint.getY() + Math.abs(distance* Math.cos(alphaInRad))) ;
					newEndPoint.setX(m_endPoint.getX() - Math.abs(distance*Math.sin(alphaInRad)));
					newEndPoint.setY(m_endPoint.getY() + Math.abs(distance* Math.cos(alphaInRad))) ;
                    
					line=new LineSegment(newStartPoint, newEndPoint);
				}
				else //sin(FalphaInRad)<0
				{
					newStartPoint.setX(m_startPoint.getX() - Math.abs(distance*Math.sin(alphaInRad)));
					newStartPoint.setY(m_startPoint.getY() - Math.abs(distance* Math.cos(alphaInRad))) ;
					newEndPoint.setX(m_endPoint.getX() - Math.abs(distance*Math.sin(alphaInRad)));
					newEndPoint.setY(m_endPoint.getY() - Math.abs(distance* Math.cos(alphaInRad))) ;
                        
					line=new LineSegment(newStartPoint, newEndPoint);
				}
			}				
		}
		return line;	
	}

	/********************************************************
	To check whether 2 lines segments have an intersection
	*********************************************************/
	public  boolean IntersectedWith(LineSegment line)
	{
		double x1=this.m_startPoint.getX();
		double y1=this.m_startPoint.getY();
		double x2=this.m_endPoint.getX();
		double y2=this.m_endPoint.getY();
		double x3=line.m_startPoint.getX();
		double y3=line.m_startPoint.getY();
		double x4=line.m_endPoint.getX();
		double y4=line.m_endPoint.getY();

		double de=(y4-y3)*(x2-x1)-(x4-x3)*(y2-y1);
		//if de<>0 then //lines are not parallel
		if (Math.abs(de-0)<smallValue) //not parallel
		{
			double ua=((x4-x3)*(y1-y3)-(y4-y3)*(x1-x3))/de;
			double ub=((x2-x1)*(y1-y3)-(y2-y1)*(x1-x3))/de;

			if ((ub> 0) && (ub<1))
				return true;
					else
				return false;
		}
		else	//lines are parallel
			return false;
	}
}
