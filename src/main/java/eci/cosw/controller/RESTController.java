package eci.cosw.controller;


import com.mongodb.client.gridfs.model.GridFSFile;
import eci.cosw.data.TodoRepository;
import eci.cosw.data.model.Todo;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.core.io.InputStreamResource;
import org.springframework.data.mongodb.core.query.Criteria;
import org.springframework.data.mongodb.core.query.Query;
import org.springframework.data.mongodb.gridfs.GridFsResource;
import org.springframework.data.mongodb.gridfs.GridFsTemplate;
import org.springframework.http.HttpStatus;
import org.springframework.http.MediaType;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;
import org.springframework.web.multipart.MultipartFile;
import org.springframework.web.servlet.mvc.support.RedirectAttributes;

import java.io.IOException;
import java.util.List;

@RequestMapping("api")
@RestController
public class RESTController {


   //TODO inject components (TodoRepository and GridFsTemplate)

    @Autowired GridFsTemplate gridFsTemplate;
    @Autowired TodoRepository todoRepository;

    @RequestMapping(value="/files/{filename}", method= RequestMethod.GET)
    public ResponseEntity<InputStreamResource> getFileByName(@PathVariable String filename) throws IOException {
        GridFSFile file = gridFsTemplate.findOne(new Query().addCriteria(Criteria.where("filename").is(filename)));

        if (file==null){
            return new ResponseEntity<>(HttpStatus.NOT_FOUND);
        }else{
            GridFsResource resource = gridFsTemplate.getResource(file.getFilename());
            System.out.print("archivo de salida: " + file);
            return ResponseEntity.ok().contentType(MediaType.valueOf(resource.getContentType())).body(new InputStreamResource(resource.getInputStream()));
        }
    }

    @CrossOrigin("*")
    @PostMapping("/files")
    public String handleFileUpload(@RequestParam("file") MultipartFile file, RedirectAttributes redirectAttributes) throws IOException {
        //Stores the file into MongoDB
        return getFileByName(file.getName()).getBody().getURL().toString();
    }

    @CrossOrigin("*")
    @PostMapping("/todo")
    public ResponseEntity<?> createTodo(@RequestBody Todo todo) {
        try {
            System.out.println(todo);
            Todo save = todoRepository.save(todo);
            return new ResponseEntity<>(HttpStatus.CREATED);
        } catch (Exception ex) {
            return new ResponseEntity<>(ex.getMessage(), HttpStatus.FORBIDDEN);
        }
    }

    @CrossOrigin("*")
    @GetMapping("/todo")
    public List<Todo> getTodoList() {
        return todoRepository.findAll();
    }

}
