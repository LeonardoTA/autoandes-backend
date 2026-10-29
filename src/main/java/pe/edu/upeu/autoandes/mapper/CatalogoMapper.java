package pe.edu.upeu.autoandes.mapper;

import org.springframework.stereotype.Component;
import pe.edu.upeu.autoandes.dto.*;
import pe.edu.upeu.autoandes.entity.*;

@Component
public class CatalogoMapper {
    public ClienteResponseDTO cliente(Cliente c) {
        return new ClienteResponseDTO(c.getId(), c.getDocumento(), c.getNombres(), c.getTelefono());
    }

    public ServicioResponseDTO servicio(Servicio s) {
        return new ServicioResponseDTO(s.getId(), s.getCodigo(), s.getNombre(), s.getPrecio(), s.getDuracionMin(), s.getActivo());
    }

    public void actualizar(Servicio s, ServicioRequestDTO d) {
        s.setCodigo(d.codigo());
        s.setNombre(d.nombre());
        s.setPrecio(d.precio());
        s.setDuracionMin(d.duracionMin());
        s.setActivo(d.activo());
    }

    public VehiculoResponseDTO vehiculo(Vehiculo v) {
        return new VehiculoResponseDTO(v.getId(), v.getPlaca(), v.getMarca(), v.getModelo(), v.getAnio(), v.getCliente().getId(), v.getCliente().getNombres());
    }

    public void actualizar(Vehiculo v, VehiculoRequestDTO d, Cliente c) {
        v.setPlaca(d.placa());
        v.setMarca(d.marca());
        v.setModelo(d.modelo());
        v.setAnio(d.anio());
        v.setCliente(c);
    }
}
