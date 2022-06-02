package br.com.zup.edu.avaliacoes.aluno;

import br.com.zup.edu.avaliacoes.compartilhado.MensagemDeErro;
import com.fasterxml.jackson.databind.ObjectMapper;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.web.servlet.AutoConfigureMockMvc;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.http.MediaType;
import org.springframework.test.context.ActiveProfiles;
import org.springframework.test.web.servlet.MockMvc;
import org.springframework.test.web.servlet.request.MockHttpServletRequestBuilder;

import java.nio.charset.StandardCharsets;
import java.util.List;

import static org.hamcrest.MatcherAssert.assertThat;
import static org.hamcrest.Matchers.containsInAnyOrder;
import static org.junit.jupiter.api.Assertions.*;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.post;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.redirectedUrlPattern;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;

@SpringBootTest
@AutoConfigureMockMvc(printOnlyOnFailure = false)
@ActiveProfiles("test")
class CadastrarAvaliacaoAoAlunoControllerTest {

    @Autowired
    private MockMvc mockMvc;

    @Autowired
    private ObjectMapper mapper;

    @Autowired
    private AlunoRepository alunoRepository;

    @Autowired
    private AvaliacaoRepository avaliacaoRepository;

    private Aluno aluno;

    @BeforeEach
    void setUp() {
        this.avaliacaoRepository.deleteAll();
        this.alunoRepository.deleteAll();
        this.aluno = new Aluno("Antonio Eloy", "antonio@email.com.br","Aceleração Acadêmica Senior");
        this.alunoRepository.save(aluno);
    }

    @Test
    @DisplayName("Deve cadastrar uma avaliação para um aluno")
    void deveCadastrarUmaAvaliacaoParaUmAluno() throws Exception {

        // Cenário
        AvaliacaoRequest avaliacaoRequest = new AvaliacaoRequest(
                "Primeira avaliação",
                "Bootcamp de Aceleração Acadêmica"
        );

        String payload = mapper.writeValueAsString(avaliacaoRequest);

        MockHttpServletRequestBuilder request = post("/alunos/{id}/avaliacoes", this.aluno.getId())
                .contentType(MediaType.APPLICATION_JSON)
                .content(payload);

        // Ação e Corretude
        mockMvc.perform(request)
                .andExpect(
                        status().isCreated()
                )
                .andExpect(
                        redirectedUrlPattern("http://localhost/alunos/*/avaliacoes/*")
                );

        // Asserts
        List<Avaliacao> avaliacoes = avaliacaoRepository.findAll();
        assertEquals(1, avaliacoes.size());

    }

    @Test
    @DisplayName("Não deve cadastrar uma avaliação para um aluno inexistente")
    void naoDeveCadastrarUmaAvaliacaoParaUmAlunoInexistente() throws Exception {

        // Cenário
        AvaliacaoRequest avaliacaoRequest = new AvaliacaoRequest(
                "Primeira avaliação",
                "Bootcamp de Aceleração Acadêmica"
        );

        String payload = mapper.writeValueAsString(avaliacaoRequest);

        MockHttpServletRequestBuilder request = post("/alunos/{id}/avaliacoes", Integer.MAX_VALUE)
                .contentType(MediaType.APPLICATION_JSON)
                .content(payload);

        // Ação e Corretude
        mockMvc.perform(request)
                .andExpect(
                        status().isNotFound()
                );

    }

    @Test
    @DisplayName("Não deve cadastrar uma avaliação com dados nulos")
    void naoDeveCadastrarUmaAvaliacaoComDadosNulos() throws Exception {

        // Cenário
        AvaliacaoRequest avaliacaoRequest = new AvaliacaoRequest(
                "",
                null
        );

        String payload = mapper.writeValueAsString(avaliacaoRequest);

        MockHttpServletRequestBuilder request = post("/alunos/{id}/avaliacoes", this.aluno.getId())
                .contentType(MediaType.APPLICATION_JSON)
                .header("Accept-Language", "pt-br")
                .content(payload);

        // Ação e Corretude
        String payloadResponse = mockMvc.perform(request)
                .andExpect(
                        status().isBadRequest()
                )
                .andReturn()
                .getResponse()
                .getContentAsString(StandardCharsets.UTF_8);

        MensagemDeErro mensagemDeErro = mapper.readValue(payloadResponse, MensagemDeErro.class);

        // Asserts
        assertEquals(2, mensagemDeErro.getMensagens().size());
        assertThat(mensagemDeErro.getMensagens(), containsInAnyOrder(
                "O campo avaliacaoReferente não deve estar em branco",
                "O campo titulo não deve estar em branco"
        ));

    }
}