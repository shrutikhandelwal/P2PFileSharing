package utilities;

// Implements C++ Style Pair found in C++ STL <utility>
public class Pair<first_type, second_type> {
	public first_type   first;
	public second_type second;
	
	public Pair(first_type first, second_type second){
		this.first = first;
		this.second = second;
	}
	
	@Override
	public boolean equals(Object o){
		Pair<?,?> p = (Pair)o;
		return (this.first.equals(p.first) && this.second.equals(p.second));
	}
	
	public boolean equals(Pair<first_type, second_type> p){
		return (this.first.equals(p.first) && this.second.equals(p.second));
	}
	
}
