package users;

import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

@Data
@AllArgsConstructor
@NoArgsConstructor
public class UserDto {
	private Integer id;
    private String name;
    private String email;
    private String phone;
    private String address;
    private String cif;
    private String sector;
    private Integer employees;
    private Integer foundedYear;
}