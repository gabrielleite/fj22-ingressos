package br.com.caelum.ingresso.validacao;

import java.time.LocalTime;
import java.time.temporal.ChronoUnit;
import java.util.Arrays;
import java.util.List;

import org.junit.Assert;
import org.junit.Test;

import br.com.caelum.ingresso.model.Filme;
import br.com.caelum.ingresso.model.Sala;
import br.com.caelum.ingresso.model.Sessao;


public class GerenciadorDeSessaoTest {
	@Test
	public void garanteQueNaoDevePermitirSessaoNoMesmoHorario() {
		Filme filme = new Filme();
		filme.setDuracao(120);

		LocalTime horario = LocalTime.now();
		Sala sala = new Sala("");

		Sessao sessao = new Sessao(horario, filme, sala);

		List<Sessao> sessoes = Arrays.asList(new Sessao(horario, filme, sala));
		GerenciadorDeSessao gerenciador = new GerenciadorDeSessao(sessoes);
		
		Assert.assertFalse(gerenciador.cabe(sessao));
	}

	@Test
	public void garanteQueNaoDevePermitirSessoesTerminandoDentroDoHorarioDeUmaSessaoJaExistente() {
		Filme filme = new Filme();
		filme.setDuracao(120);

		LocalTime horario = LocalTime.now();
		Sala sala = new Sala("");

		List<Sessao> sessoes = Arrays.asList(new Sessao(horario.plusHours(1), filme, sala));
		GerenciadorDeSessao gerenciador = new GerenciadorDeSessao(sessoes);
		
		Sessao sessao = new Sessao(horario, filme, sala);

		Assert.assertFalse(gerenciador.cabe(sessao));
	}

	@Test
	public void garanteQueNaoDevePermitirSessoesIniciandoDentroDoHorarioDeUmaSessaoJaExistente() {
		Filme filme = new Filme();
		filme.setDuracao(120);

		LocalTime horario = LocalTime.now();
		Sala sala = new Sala("");

		List<Sessao> sessoesDaSala = Arrays.asList(new Sessao(horario, filme, sala));
		GerenciadorDeSessao gerenciador = new GerenciadorDeSessao(sessoesDaSala);

		Sessao sessao = new Sessao(horario.plus(1, ChronoUnit.HOURS), filme, sala);

		Assert.assertFalse(gerenciador.cabe(sessao));
	}

	@Test
	public void garanteQueDevePermitirUmaInsercaoEntreDoisFilmes() {
		Sala sala = new Sala("");
		
		Filme filme1 = new Filme();
		filme1.setDuracao(90);
		
		LocalTime dezHoras = LocalTime.parse("10:00:00");
		Sessao sessaoDasDez = new Sessao(dezHoras, filme1, sala);
		
		Filme filme2 = new Filme();
		filme2.setDuracao(120);
		
		LocalTime dezoitoHoras = LocalTime.parse("18:00:00");
		Sessao sessaoDasDezoito = new Sessao(dezoitoHoras, filme2, sala);
		
		List<Sessao> sessoes = Arrays.asList(sessaoDasDez, sessaoDasDezoito);
		GerenciadorDeSessao gerenciador = new GerenciadorDeSessao(sessoes);
		
		Assert.assertTrue(gerenciador.cabe(new Sessao(LocalTime.parse("13:00:00"), filme2, sala)));
	}
}
