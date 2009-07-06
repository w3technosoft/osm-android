package com.google.code.osmandroidconverter.polygons;

public class Point {
	private double m_dCoordinate_X;
	private double m_dCoordinate_Y;
	
	private static double smallValue = 0.00001;
	private static double bigValue   = 99999;
	
	public Point(){
	
	}

	public Point(double xCoordinate, double yCoordinate)
	{
		m_dCoordinate_X=xCoordinate;
		m_dCoordinate_Y=yCoordinate;
	}
	
	public void setX(double value)
	{
		m_dCoordinate_X=value;
	}
	
	public double getX()
	{		
		return m_dCoordinate_X;
	}
	
	public void setY(double value)
	{
		m_dCoordinate_Y=value;
	}
	
	public double getY()
	{		
		return m_dCoordinate_Y;
	}	
	
	public static boolean SamePoints(Point point1,Point point2){
		
		double dDeff_X=	Math.abs(point1.getX()-point2.getX());
		double dDeff_Y= Math.abs(point1.getY()-point2.getY());

		if ((dDeff_X < smallValue) && (dDeff_Y< smallValue))
			return true;
		else
			return false;
	}
		
	public boolean EqualsPoint(Point newPoint){
		
		double dDeff_X=	Math.abs(m_dCoordinate_X-newPoint.getX());
		double dDeff_Y= Math.abs(m_dCoordinate_Y-newPoint.getY());

		if ((dDeff_X<smallValue) && (dDeff_Y<smallValue))
			return true;
		else
			return false;
	}
		
	/***To check whether the point is in a line segment***/
	public boolean InLine(LineSegment lineSegment){
		boolean bInline=false;

		double Ax, Ay, Bx, By, Cx, Cy;
		Bx=lineSegment.getEndPoint().getX();
		By=lineSegment.getEndPoint().getY();
		Ax=lineSegment.getStartPoint().getX();
		Ay=lineSegment.getStartPoint().getY();
		Cx=this.m_dCoordinate_X;
		Cy=this.m_dCoordinate_Y;
  
		double L=lineSegment.GetLineSegmentLength();
		double s=Math.abs(((Ay-Cy)*(Bx-Ax)-(Ax-Cx)*(By-Ay))/(L*L));
  
		if (Math.abs(s-0)<smallValue){
				if ((SamePoints(this, lineSegment.getStartPoint())) ||
					(SamePoints(this, lineSegment.getEndPoint())))
					bInline=true;
				else if ((Cx<lineSegment.GetXmax())
					&& (Cx>lineSegment.GetXmin())
					&&(Cy< lineSegment.GetYmax())
					&& (Cy>lineSegment.GetYmin()))
					bInline=true;
			}
			return bInline;
	}

	/*** Distance between two points***/
	public double DistanceTo(Point point){
		return Math.sqrt((point.getX()-this.getX())*(point.getX()-this.getX()) 
				+ (point.getY()-this.getY())*(point.getY()-this.getY()));

	}

	public boolean PointInsidePolygon(Point[] polygonVertices){
		if (polygonVertices.length<3) //not a valid polygon
			return false;
			
		int  nCounter= 0;
		int nPoints = polygonVertices.length;
			
		Point s1, p1, p2;
		s1 = this;
		p1= polygonVertices[0];
			
		for (int i= 1; i<nPoints; i++){
			
			p2= polygonVertices[i % nPoints];
			if (s1.getY() > Math.min(p1.getY(), p2.getY())){
				
				if (s1.getY() <= Math.max(p1.getY(), p2.getY()) ){
					
					if (s1.getX() <= Math.max(p1.getX(), p2.getX()) ){
					
						if (p1.getY() != p2.getY()){
							double xInters = (s1.getY() - p1.getY()) * (p2.getX() - p1.getX()) /
								(p2.getY() - p1.getY()) + p1.getX();
						
							if ((p1.getX()== p2.getX()) || (s1.getX() <= xInters) ){
								nCounter ++;
							}
						}  //p1.y != p2.y
					}
				}
			}
			p1 = p2;
		} //for loop
  
		if ((nCounter % 2) == 0) 
			return false;
		else
			return true;
	}
		
	/*********** Sort points from Xmin->Xmax ******/
	public static void SortPointsByX(Point[] points){
		if (points.length>1){
			Point tempPt;
			for (int i=0; i< points.length-2; i++){
				
				for (int j = i+1; j < points.length -1; j++){
					
					if (points[i].getX() > points[j].getX()){
						tempPt= points[j];
						points[j]=points[i];
						points[i]=tempPt;
					}
				}
			}
		}
	}
		
	/*********** Sort points from Ymin->Ymax ******/
	public static void SortPointsByY(Point[] points){
		if (points.length>1){
			Point tempPt;
			for (int i=0; i< points.length-2; i++){
				for (int j = i+1; j < points.length -1; j++){
					
					if (points[i].getY() > points[j].getY()){
						tempPt= points[j];
						points[j]=points[i];
						points[i]=tempPt;
					}
				}
			}
		}
	}
		
}
