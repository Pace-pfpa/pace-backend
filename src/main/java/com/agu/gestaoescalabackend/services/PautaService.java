package com.agu.gestaoescalabackend.services;

import com.agu.gestaoescalabackend.dto.MutiraoDTO;
import com.agu.gestaoescalabackend.dto.PautaDto;
import com.agu.gestaoescalabackend.entities.Mutirao;
import com.agu.gestaoescalabackend.entities.Pauta;
import com.agu.gestaoescalabackend.enums.StatusPauta;
import com.agu.gestaoescalabackend.repositories.MutiraoRepository;
import com.agu.gestaoescalabackend.repositories.PautaRepository;
import com.agu.gestaoescalabackend.repositories.PautistaRepository;
import lombok.AllArgsConstructor;

import org.springframework.data.domain.Page;
import org.springframework.data.domain.PageImpl;
import org.springframework.data.domain.PageRequest;
import org.springframework.data.domain.Pageable;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.time.LocalDate;
import java.util.ArrayList;
import java.util.List;
import java.util.Optional;
import java.util.stream.Collectors;

@Service
@AllArgsConstructor
public class PautaService {

	private PautaRepository pautaRepository;
	private PautistaRepository pautistaRepository;
	private MutiraoRepository mutiraoRepository;
	private MutiraoService mutiraoService;

//////////////////////////////////   SERVIÇOS   ///////////////////////////////////

	@Transactional(readOnly = true)
	public Page<PautaDto> findAll(int page, int size) {
		Pageable pageable = PageRequest.of(page, size);
		int totalPages = pautaRepository.findAll(pageable).getTotalPages();
		List<PautaDto> response = pautaRepository.findAll(pageable)
		.stream()
		.map(Pauta::toDto)
		.collect(Collectors.toList());
			return new PageImpl<PautaDto>(response);
			
	}

	@Transactional(readOnly = true)
	public PautaDto findById(Long id) {

		return pautaRepository.findById(id)
				.map(Pauta::toDto)
				.orElse(null);
	}

	@Transactional
	public List<PautaDto> save(List<PautaDto> listaPautaDto) {

		Mutirao mutirao = mutiraoService.save(listaPautaDto).toEntity();

		for (PautaDto pautaDto : listaPautaDto) {

			Pauta pauta = pautaDto.toEntity();
			pauta.setMutirao(mutirao);

			if (validarCriacao(pautaDto, pauta)) {
				pautaRepository.save(pauta).toDto();
			}
		}
		return pautaRepository.findAllByMutiraoId(mutirao.getId())
				.stream()
				.map(Pauta::toDto)
				.collect(Collectors.toList());
	}

	@Transactional
	public PautaDto editar(Long pautaDeAudienciaId, PautaDto pautaDto) {

		Optional<Pauta> pautaOptional = pautaRepository.findById(pautaDeAudienciaId);

		if (pautaOptional.isEmpty())
			return null;
		
		Pauta pauta = pautaOptional.get().forUpdate(pautaDto);

		pauta = pautaRepository.save(pauta);
		return pauta.toDto();
	}

	@Transactional
	public void excluir(Long pautaDeAudienciaId) {
		if (pautaRepository.existsById(pautaDeAudienciaId)){

			Optional<Pauta> pautaOptional = pautaRepository.findById(pautaDeAudienciaId);
			if(pautaOptional.isPresent()){
				Integer quantidadeDePautas = pautaOptional.get().getMutirao().getQuantidaDePautas();
				if (quantidadeDePautas == 1){
					mutiraoRepository.deleteById(pautaOptional.get().getMutirao().getId());
				}
			}
			pautaRepository.deleteById(pautaDeAudienciaId);
		}
	}

	/*------------------------------------------------
     METODOS DO MUTIRAO
    ------------------------------------------------*/

	private boolean validarCriacao(PautaDto pautaDto, Pauta pauta) {
		// Instancia um objeto base para verificar se já existe um registro 'nome'
		// no banco igual ao do DTO
		Pauta pautaExistente = pautaRepository.findByProcessoAndTipoPauta(pautaDto.getProcesso(),
				pautaDto.getTipoPauta());
		return (pautaExistente == null || pautaExistente.equals(pauta));
	}
}
