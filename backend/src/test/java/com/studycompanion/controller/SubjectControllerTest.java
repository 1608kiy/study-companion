package com.studycompanion.controller;

import com.studycompanion.dto.CreateSubjectRequest;
import com.studycompanion.common.JwtUtil;
import com.studycompanion.service.SubjectService;
import com.studycompanion.vo.SubjectVO;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.http.MediaType;
import org.springframework.test.web.servlet.MockMvc;
import org.springframework.test.web.servlet.setup.MockMvcBuilders;

import java.util.Arrays;

import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.when;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.*;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.*;

@ExtendWith(MockitoExtension.class)
class SubjectControllerTest {

    private MockMvc mockMvc;

    @Mock
    private SubjectService subjectService;

    @Mock
    private JwtUtil jwtUtil;

    @InjectMocks
    private SubjectController subjectController;

    @BeforeEach
    void setUp() {
        mockMvc = MockMvcBuilders.standaloneSetup(subjectController).build();
    }

    @Test
    void getSubjectList_Success() throws Exception {
        SubjectVO subject = new SubjectVO();
        subject.setId(1L);
        subject.setName("行测");
        subject.setColor("#409EFF");

        when(jwtUtil.getUserIdFromToken(any())).thenReturn(1L);
        when(subjectService.getSubjectList(1L)).thenReturn(Arrays.asList(subject));

        mockMvc.perform(get("/api/v1/subjects")
                .header("Authorization", "Bearer test-token"))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.data[0].name").value("行测"));
    }

    @Test
    void createSubject_Success() throws Exception {
        CreateSubjectRequest request = new CreateSubjectRequest();
        request.setName("申论");
        request.setColor("#67C23A");

        SubjectVO result = new SubjectVO();
        result.setId(2L);
        result.setName("申论");
        result.setColor("#67C23A");

        when(jwtUtil.getUserIdFromToken(any())).thenReturn(1L);
        when(subjectService.createSubject(any(), any(CreateSubjectRequest.class))).thenReturn(result);

        mockMvc.perform(post("/api/v1/subjects")
                .header("Authorization", "Bearer test-token")
                .contentType(MediaType.APPLICATION_JSON)
                .content("{\"name\":\"申论\",\"color\":\"#67C23A\"}"))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.data.name").value("申论"));
    }

    @Test
    void createSubject_EmptyName_ReturnsBadRequest() throws Exception {
        mockMvc.perform(post("/api/v1/subjects")
                .header("Authorization", "Bearer test-token")
                .contentType(MediaType.APPLICATION_JSON)
                .content("{\"name\":\"\",\"color\":\"#67C23A\"}"))
                .andExpect(status().isBadRequest());
    }

    @Test
    void deleteSubject_Success() throws Exception {
        when(jwtUtil.getUserIdFromToken(any())).thenReturn(1L);

        mockMvc.perform(delete("/api/v1/subjects/1")
                .header("Authorization", "Bearer test-token"))
                .andExpect(status().isOk());
    }
}
