package semantic.search.query;

public class Annotation {
	
	String keywords;
	String annotation;
	float score;
	
	public Annotation(String keywords, String annotation, float score){
		this.keywords = keywords;
		this.annotation = annotation;
		this.score = score;	
	}

	public void setScore(float score){
		this.score = score;
	}
	public String getAnnotation(){
		return annotation;
	}
	public String getKeyword(){
		return keywords;
	}
	public float getScore(){
		return score;
	}
	public String toString(){
		return keywords+" "+annotation+" "+score;
	}
}
