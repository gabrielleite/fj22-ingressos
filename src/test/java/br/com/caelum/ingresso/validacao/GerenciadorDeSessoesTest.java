package br.com.caelum.ingresso.validacao;

import java.math.BigDecimal;
import java.time.Duration;
import java.time.LocalTime;
import java.time.temporal.ChronoUnit;
import java.util.Arrays;
import java.util.List;

import org.junit.Assert;
import org.junit.Test;

import br.com.caelum.ingresso.model.Filme;
import br.com.caelum.ingresso.model.Sala;
import br.com.caelum.ingresso.model.Sessao;


/*
 * # ERRATA
 * 
 * Há um problema sutil na modelagem do sistema que os testes só pegam dependendo
 * do horário do dia em que são executados! A classe LocalTime utilizada para
 * representar o horário de início e término das sessões não leva em conta os dia,
 * apenas o horário. Isso quer dizer que a comparação feita dentro do
 * GerenciadorDeSessoes pode falhar inesperadamente e nos retornar resultados
 * inconsistentes caso as sessões tenham seu horário de início muito próximo ao horário
 * de virada do dia.
 * 
 * - Ex. prático:
 * 
 * Um dos retornos do método cabe() da classe GerenciadorDeSessoes é:
 * 		return sessaoExistente
 *					.getHorarioTermino()
 *					.isBefore(horarioAtual);
 *
 * O qual retorna se o horário de término (horário início + duração do filme) é anterior
 * ao horário de início da sessão que se deseja cadastrar. Pois bem, caso a sessão
 * existente se inicie às 22:30 e tenha um filme de 2h de duração, seu término será às
 * 00:30. Se horarioAtual que representa o horário de início da sessão a ser inserida
 * for, por exemplo, 22:30, o Java ao executar a linha de código acima irá retornar true
 * afinal de contas 00:30 é antes de 22:30, possibilitando assim o cadastro de sessões
 * que tem seus horários conflitantes. Mas não era isso que esperávamos!
 * Esse problema ocorre justamente por a classe LocalTime não levar em consideração o
 * dia corrente.
 * 
 * Nesse momento vocês devem estar se perguntando: Por que em nenhum momento do curso
 * tivemos esse problema nos testes?
 * 
 * A resposta é simples! O código de teste, como pode ser constatado abaixo, cria uma
 * instância de LocalTime usando o método estático now() que retorna um horário do exato
 * momento em que o código é executado. Como o curso ocorreu durante os o horário de
 * 9h às 17h não vimos esse erro ocorrer por estarmos longe do horário de virada de dia
 * (0h).
 * 
 * Para atestar o problema, experimente, por exemplo, no método de teste
 * garanteQueNaoDevePermitirSessaoNoMesmoHorario(), trocar a linha
 * 		LocalTime horario = LocalTime.now();
 * por
 * 		LocalTime horario = LocalTime.of(22, 00);
 * 
 * e veja que o teste irá falhar!
 * 
 * Se, por outro lado, trocar por
 * 		LocalTime horario = LocalTime.of(9, 00);
 * 
 * o teste volta a passar! Comprovando assim a falha na nossa modelagem, pois o nosso
 * sistema não deveria ficar refém do horário em que ele está sendo executado para ter
 * seu funcionamento garantido.
 * 
 * 
 * ## SOLUÇÃO 1
 * 
 * Para solucionar o problema percebido basta trocar a classe LocalTime utilizada na
 * classe Sessao para representar o horário da sessão por uma classe que leve em
 * consideração também o dia, como a classe LocalDateTime
 * (link para o Javadoc da classe: https://docs.oracle.com/javase/8/docs/api/java/time/LocalDateTime.html).
 * 
 * Claro que ao efetuar essa mudança, outras mudanças deverão ser feitas pelo sistema
 * uma vez que em outras partes do sistema era esperado a utilização de um LocalTime
 * na classe da sessão e não um LocalDateTime.
 * 
 * ## SOLUÇÃO 2
 * 
 * Uma solução menos invasiva seria fixar um dia apenas para fins de comparação e
 * verificação dentro da classe GerenciadorDeSessoes de modo que o método
 * horarioIsValido fique da seguinte forma:
 * 
 *	private boolean horarioIsValido(Sessao sessaoExistente, Sessao sessaoAtual) {
 *		
 *		// Cria um objeto LocalDate com a data atual.
 *		LocalDate dataAtual = LocalDate.now();
 *
 *		// Junta o horário e a data num único objeto LocalDateTime que leva em
 *		// consideração tanto a data quanto o horário.
 *		LocalDateTime horarioSessao = sessaoExistente.getHorario().atDate(dataAtual);
 *		LocalDateTime horarioAtual = sessaoAtual.getHorario().atDate(dataAtual);
 *		
 *		boolean ehAntes = horarioAtual.isBefore(horarioSessao);
 *		
 *		if (ehAntes) {
 *			Duration duracaoSessaoAtual = sessaoAtual
 *												.getFilme()
 *												.getDuracao();
 *			
 *			return horarioAtual
 *					.plusMinutes(duracaoSessaoAtual.toMinutes())
 *					.isBefore(horarioSessao);
 *		} else {
 *			Duration duracaoSessaoExistente = sessaoExistente
 *														.getFilme()
 *														.getDuracao();
 *			
 *			LocalDateTime horarioTermino = horarioSessao
 *													.plus(duracaoSessaoExistente);
 *			
 *			return horarioTermino
 *					.isBefore(horarioAtual);
 *		}
 *	}
 * 
 * Dessa forma, nossa verificação irá passar a levar em consideração também o dia
 * resolvendo assim o problema do cadastro de sessões no limiar do dia e sem ter que
 * mudar outras partes do sistema.
 * 
 * No entanto, o sistema irá considerar que todas as sessões cadastradas irão ocorrer
 * no mesmo dia e que se repetirão nos dias subsequentes.
 * 
 * 
 * ## Conclusão
 * 
 * De modo geral, a solução 2 é boa. No entanto, a solução 1 traria maior flexibilidade
 * ao sistema por permitir sessões de horários diferentes em dias diferentes. O que irá
 * definir qual abordagem usar será a regra de negócio que se busca aplicar.
 * 
 */
public class GerenciadorDeSessoesTest {
	@Test
	public void garanteQueNaoDevePermitirSessaoNoMesmoHorario() {
		Filme filme = new Filme("Rogue One", Duration.ofMinutes(120), "SCI-FI", BigDecimal.ONE);

		LocalTime horario = LocalTime.now();
		Sala sala = new Sala("Eldorado - IMAX", BigDecimal.ONE);

		Sessao sessao = new Sessao(horario, filme, sala);

		List<Sessao> sessoes = Arrays.asList(new Sessao(horario, filme, sala));
		GerenciadorDeSessoes gerenciador = new GerenciadorDeSessoes(sessoes);
		
		Assert.assertFalse(gerenciador.cabe(sessao));
	}

	/*
	 * # ERRATA
	 * 
	 * A apostila coloca o seguinte trecho de código:
	 * 
	 * List<Sessao> sessoes = Arrays.asList(new Sessao(horario, filme, sala));
	 * 
	 * Sessao sessao = new Sessao(horario.plusHours(1), filme, sala);
	 * 
	 * No entanto, o código está incorreto, pois nesse caso a nova sessão
	 * INICIARIA durante uma sessão que existente. Mas, o que se pretende
	 * testar é o TÉRMINO durante uma sessão existente.
	 * Portanto, o código correto é o que se segue.
	 * 
	 * List<Sessao> sessoes = Arrays.asList(new Sessao(horario.plusHours(1), filme, sala));
	 * 
	 * Sessao sessao = new Sessao(horario, filme, sala);
	 * 
	 */
	@Test
	public void garanteQueNaoDevePermitirSessoesTerminandoDentroDoHorarioDeUmaSessaoJaExistente() {
		Filme filme = new Filme("Rogue One", Duration.ofMinutes(120), "SCI-FI", BigDecimal.ONE);

		LocalTime horario = LocalTime.now();
		Sala sala = new Sala("Eldorado - IMAX", BigDecimal.ONE);

		List<Sessao> sessoes = Arrays.asList(new Sessao(horario.plusHours(1), filme, sala));
		GerenciadorDeSessoes gerenciador = new GerenciadorDeSessoes(sessoes);
		
		Sessao sessao = new Sessao(horario, filme, sala);

		Assert.assertFalse(gerenciador.cabe(sessao));
	}

	@Test
	public void garanteQueNaoDevePermitirSessoesIniciandoDentroDoHorarioDeUmaSessaoJaExistente() {
		Filme filme = new Filme("Rogue One", Duration.ofMinutes(120), "SCI-FI", BigDecimal.ONE);

		LocalTime horario = LocalTime.now();
		Sala sala = new Sala("Eldorado - IMAX", BigDecimal.ONE);

		List<Sessao> sessoesDaSala = Arrays.asList(new Sessao(horario, filme, sala));
		GerenciadorDeSessoes gerenciador = new GerenciadorDeSessoes(sessoesDaSala);

		Sessao sessao = new Sessao(horario.plus(1, ChronoUnit.HOURS), filme, sala);

		Assert.assertFalse(gerenciador.cabe(sessao));
	}

	@Test
	public void garanteQueDevePermitirUmaInsercaoEntreDoisFilmes() {
		Sala sala = new Sala("Eldorado - IMAX", BigDecimal.ONE);
		
		Filme filme1 = new Filme("Rogue One", Duration.ofMinutes(120), "SCI-FI", BigDecimal.ONE);
		
		LocalTime dezHoras = LocalTime.parse("10:00:00");
		Sessao sessaoDasDez = new Sessao(dezHoras, filme1, sala);
		
		Filme filme2 = new Filme("Rogue One", Duration.ofMinutes(120), "SCI-FI", BigDecimal.ONE);
		
		LocalTime dezoitoHoras = LocalTime.parse("18:00:00");
		Sessao sessaoDasDezoito = new Sessao(dezoitoHoras, filme2, sala);
		
		List<Sessao> sessoes = Arrays.asList(sessaoDasDez, sessaoDasDezoito);
		GerenciadorDeSessoes gerenciador = new GerenciadorDeSessoes(sessoes);
		
		Assert.assertTrue(gerenciador.cabe(new Sessao(LocalTime.parse("13:00:00"), filme2, sala)));
	}
}
