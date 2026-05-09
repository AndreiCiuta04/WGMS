package at.ac.tuwien.sepr.groupphase.backend.integrationtest;

import at.ac.tuwien.sepr.groupphase.backend.basetest.TestData;
import at.ac.tuwien.sepr.groupphase.backend.config.properties.SecurityProperties;
import at.ac.tuwien.sepr.groupphase.backend.endpoint.dto.TaskCreateDto;
import at.ac.tuwien.sepr.groupphase.backend.endpoint.dto.TaskDetailDto;
import at.ac.tuwien.sepr.groupphase.backend.endpoint.dto.TaskStatusUpdateDto;
import at.ac.tuwien.sepr.groupphase.backend.entity.Task;
import at.ac.tuwien.sepr.groupphase.backend.entity.TaskStatus;
import at.ac.tuwien.sepr.groupphase.backend.repository.TaskRepository;
import at.ac.tuwien.sepr.groupphase.backend.security.JwtTokenizer;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.boot.webmvc.test.autoconfigure.AutoConfigureMockMvc;
import org.springframework.http.HttpStatus;
import org.springframework.http.MediaType;
import org.springframework.mock.web.MockHttpServletResponse;
import org.springframework.test.context.ActiveProfiles;
import org.springframework.test.context.junit.jupiter.SpringExtension;
import org.springframework.test.web.servlet.MockMvc;
import org.springframework.test.web.servlet.MvcResult;
import tools.jackson.databind.json.JsonMapper;

import java.util.Arrays;
import java.util.List;

import static org.junit.jupiter.api.Assertions.assertAll;
import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertNotNull;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.delete;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.get;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.patch;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.post;
import static org.springframework.test.web.servlet.result.MockMvcResultHandlers.print;

@ExtendWith(SpringExtension.class)
@SpringBootTest(webEnvironment = SpringBootTest.WebEnvironment.RANDOM_PORT)
@ActiveProfiles("test")
@AutoConfigureMockMvc
public class TaskEndpointTest implements TestData {

    @Autowired
    private MockMvc mockMvc;

    @Autowired
    private TaskRepository taskRepository;

    @Autowired
    private JwtTokenizer jwtTokenizer;

    @Autowired
    private SecurityProperties securityProperties;

    @Autowired
    private JsonMapper jsonMapper;

    @BeforeEach
    public void beforeEach() {
        taskRepository.deleteAll();
    }

    @Test
    public void givenUserLoggedIn_whenCreateTask_then201AndTaskIsCreated() throws Exception {
        TaskCreateDto taskCreateDto = new TaskCreateDto();
        taskCreateDto.setTitle(TEST_TASK_TITLE);
        taskCreateDto.setDescription(TEST_TASK_DESCRIPTION);
        taskCreateDto.setDueDate(TEST_TASK_DUE_DATE);
        taskCreateDto.setAssignedToEmail(ADMIN_USER);

        String body = jsonMapper.writeValueAsString(taskCreateDto);

        MvcResult mvcResult = this.mockMvc.perform(post(TASK_BASE_URI)
                .contentType(MediaType.APPLICATION_JSON)
                .content(body)
                .header(securityProperties.getAuthHeader(), jwtTokenizer.getAuthToken(DEFAULT_USER, USER_ROLES)))
            .andDo(print())
            .andReturn();

        MockHttpServletResponse response = mvcResult.getResponse();

        assertEquals(HttpStatus.CREATED.value(), response.getStatus());
        assertEquals(MediaType.APPLICATION_JSON_VALUE, response.getContentType());

        TaskDetailDto taskDetailDto = jsonMapper.readValue(response.getContentAsString(), TaskDetailDto.class);

        assertAll(
            () -> assertNotNull(taskDetailDto.getId()),
            () -> assertEquals(TEST_TASK_TITLE, taskDetailDto.getTitle()),
            () -> assertEquals(TEST_TASK_DESCRIPTION, taskDetailDto.getDescription()),
            () -> assertEquals(TEST_TASK_DUE_DATE, taskDetailDto.getDueDate()),
            () -> assertEquals(TaskStatus.OPEN, taskDetailDto.getStatus()),
            () -> assertEquals(DEFAULT_USER, taskDetailDto.getCreatedByEmail()),
            () -> assertEquals(ADMIN_USER, taskDetailDto.getAssignedToEmail()),
            () -> assertNotNull(taskDetailDto.getCreatedAt()),
            () -> assertNotNull(taskDetailDto.getUpdatedAt())
        );

        assertEquals(1, taskRepository.findAll().size());
    }

    @Test
    public void givenNoOneLoggedIn_whenCreateTask_then403() throws Exception {
        TaskCreateDto taskCreateDto = new TaskCreateDto();
        taskCreateDto.setTitle(TEST_TASK_TITLE);
        taskCreateDto.setDescription(TEST_TASK_DESCRIPTION);
        taskCreateDto.setDueDate(TEST_TASK_DUE_DATE);
        taskCreateDto.setAssignedToEmail(ADMIN_USER);

        String body = jsonMapper.writeValueAsString(taskCreateDto);

        MvcResult mvcResult = this.mockMvc.perform(post(TASK_BASE_URI)
                .contentType(MediaType.APPLICATION_JSON)
                .content(body))
            .andDo(print())
            .andReturn();

        MockHttpServletResponse response = mvcResult.getResponse();

        assertEquals(HttpStatus.FORBIDDEN.value(), response.getStatus());
    }

    @Test
    public void givenInvalidTask_whenCreateTask_then400() throws Exception {
        TaskCreateDto taskCreateDto = new TaskCreateDto();
        taskCreateDto.setTitle(null);
        taskCreateDto.setDescription(TEST_TASK_DESCRIPTION);
        taskCreateDto.setDueDate(null);
        taskCreateDto.setAssignedToEmail("not-an-email");

        String body = jsonMapper.writeValueAsString(taskCreateDto);

        MvcResult mvcResult = this.mockMvc.perform(post(TASK_BASE_URI)
                .contentType(MediaType.APPLICATION_JSON)
                .content(body)
                .header(securityProperties.getAuthHeader(), jwtTokenizer.getAuthToken(DEFAULT_USER, USER_ROLES)))
            .andDo(print())
            .andReturn();

        MockHttpServletResponse response = mvcResult.getResponse();

        assertEquals(HttpStatus.BAD_REQUEST.value(), response.getStatus());
    }

    @Test
    public void givenTaskAssignedToUser_whenFindMyTasks_thenReturnAssignedTask() throws Exception {
        Task task = Task.TaskBuilder.aTask()
            .withTitle(TEST_TASK_TITLE)
            .withDescription(TEST_TASK_DESCRIPTION)
            .withDueDate(TEST_TASK_DUE_DATE)
            .withStatus(TaskStatus.OPEN)
            .withCreatedByEmail(ADMIN_USER)
            .withAssignedToEmail(DEFAULT_USER)
            .build();

        taskRepository.save(task);

        MvcResult mvcResult = this.mockMvc.perform(get(TASK_BASE_URI + "/my")
                .header(securityProperties.getAuthHeader(), jwtTokenizer.getAuthToken(DEFAULT_USER, USER_ROLES)))
            .andDo(print())
            .andReturn();

        MockHttpServletResponse response = mvcResult.getResponse();

        assertEquals(HttpStatus.OK.value(), response.getStatus());
        assertEquals(MediaType.APPLICATION_JSON_VALUE, response.getContentType());

        List<TaskDetailDto> tasks = Arrays.asList(jsonMapper.readValue(response.getContentAsString(), TaskDetailDto[].class));

        assertEquals(1, tasks.size());
        assertEquals(TEST_TASK_TITLE, tasks.get(0).getTitle());
        assertEquals(DEFAULT_USER, tasks.get(0).getAssignedToEmail());
    }

    @Test
    public void givenTaskAssignedToUser_whenUpdateStatus_then200AndStatusChanged() throws Exception {
        Task task = Task.TaskBuilder.aTask()
            .withTitle(TEST_TASK_TITLE)
            .withDescription(TEST_TASK_DESCRIPTION)
            .withDueDate(TEST_TASK_DUE_DATE)
            .withStatus(TaskStatus.OPEN)
            .withCreatedByEmail(ADMIN_USER)
            .withAssignedToEmail(DEFAULT_USER)
            .build();

        Task savedTask = taskRepository.save(task);

        TaskStatusUpdateDto taskStatusUpdateDto = new TaskStatusUpdateDto();
        taskStatusUpdateDto.setStatus(TaskStatus.DONE);

        String body = jsonMapper.writeValueAsString(taskStatusUpdateDto);

        MvcResult mvcResult = this.mockMvc.perform(patch(TASK_BASE_URI + "/{id}/status", savedTask.getId())
                .contentType(MediaType.APPLICATION_JSON)
                .content(body)
                .header(securityProperties.getAuthHeader(), jwtTokenizer.getAuthToken(DEFAULT_USER, USER_ROLES)))
            .andDo(print())
            .andReturn();

        MockHttpServletResponse response = mvcResult.getResponse();

        assertEquals(HttpStatus.OK.value(), response.getStatus());

        TaskDetailDto taskDetailDto = jsonMapper.readValue(response.getContentAsString(), TaskDetailDto.class);

        assertEquals(TaskStatus.DONE, taskDetailDto.getStatus());
    }

    @Test
    public void givenCreatorLoggedIn_whenDeleteTask_then204AndTaskDeleted() throws Exception {
        Task task = Task.TaskBuilder.aTask()
            .withTitle(TEST_TASK_TITLE)
            .withDescription(TEST_TASK_DESCRIPTION)
            .withDueDate(TEST_TASK_DUE_DATE)
            .withStatus(TaskStatus.OPEN)
            .withCreatedByEmail(DEFAULT_USER)
            .withAssignedToEmail(ADMIN_USER)
            .build();

        Task savedTask = taskRepository.save(task);

        MvcResult mvcResult = this.mockMvc.perform(delete(TASK_BASE_URI + "/{id}", savedTask.getId())
                .header(securityProperties.getAuthHeader(), jwtTokenizer.getAuthToken(DEFAULT_USER, USER_ROLES)))
            .andDo(print())
            .andReturn();

        MockHttpServletResponse response = mvcResult.getResponse();

        assertEquals(HttpStatus.NO_CONTENT.value(), response.getStatus());
        assertEquals(0, taskRepository.findAll().size());
    }

    @Test
    public void givenAdminLoggedIn_whenFindAllTasks_thenAdminSeesAllTasks() throws Exception {
        Task taskOne = Task.TaskBuilder.aTask()
            .withTitle("Kitchen")
            .withDescription("Clean kitchen")
            .withDueDate(TEST_TASK_DUE_DATE)
            .withStatus(TaskStatus.OPEN)
            .withCreatedByEmail(DEFAULT_USER)
            .withAssignedToEmail(DEFAULT_USER)
            .build();

        Task taskTwo = Task.TaskBuilder.aTask()
            .withTitle("Bathroom")
            .withDescription("Clean bathroom")
            .withDueDate(TEST_TASK_DUE_DATE.plusDays(1))
            .withStatus(TaskStatus.OPEN)
            .withCreatedByEmail(ADMIN_USER)
            .withAssignedToEmail(ADMIN_USER)
            .build();

        taskRepository.save(taskOne);
        taskRepository.save(taskTwo);

        MvcResult mvcResult = this.mockMvc.perform(get(TASK_BASE_URI)
                .header(securityProperties.getAuthHeader(), jwtTokenizer.getAuthToken(ADMIN_USER, ADMIN_ROLES)))
            .andDo(print())
            .andReturn();

        MockHttpServletResponse response = mvcResult.getResponse();

        assertEquals(HttpStatus.OK.value(), response.getStatus());

        List<TaskDetailDto> tasks = Arrays.asList(jsonMapper.readValue(response.getContentAsString(), TaskDetailDto[].class));

        assertEquals(2, tasks.size());
    }
}