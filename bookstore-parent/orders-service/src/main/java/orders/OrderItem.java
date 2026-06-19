package orders;

import jakarta.persistence.Embeddable;

@Embeddable
public class OrderItem {
    
    private Long bookId;
    private Integer quantity;
    private Double price;
    
    public Long getBookId() { 
    	return bookId; 
    }

	public void setBookId(Long bookId) {
		this.bookId = bookId;
	}

	public Integer getQuantity() {
		return quantity;
	}

	public void setQuantity(Integer quantity) {
		this.quantity = quantity;
	}

	public Double getPrice() {
		return price;
	}

	public void setPrice(Double price) {
		this.price = price;
	}
}