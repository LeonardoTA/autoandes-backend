package pe.edu.upeu.autoandes.service.impl;

import java.util.List;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.data.domain.Sort;
import pe.edu.upeu.autoandes.service.ClienteService;
import pe.edu.upeu.autoandes.repository.ClienteRepository;
import pe.edu.upeu.autoandes.mapper.CatalogoMapper;
import pe.edu.upeu.autoandes.dto.ClienteResponseDTO;
@Service @RequiredArgsConstructor @Transactional(readOnly=true)
public class ClienteServiceImpl implements ClienteService {
 private final ClienteRepository clientes;private final CatalogoMapper mapper;
 public List<ClienteResponseDTO> listar() {return clientes.findAll(Sort.by("nombres")).stream().map(mapper::cliente).toList();}
}
