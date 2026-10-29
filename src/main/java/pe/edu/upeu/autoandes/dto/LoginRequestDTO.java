package pe.edu.upeu.autoandes.dto;

public record LoginRequestDTO(@jakarta.validation.constraints.NotBlank @jakarta.validation.constraints.Size(max=30) String username,@jakarta.validation.constraints.NotBlank @jakarta.validation.constraints.Size(max=72) String password) {}
