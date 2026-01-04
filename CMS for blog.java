package com.cms;

import org.springframework.boot.SpringApplication;
import org.springframework.boot.autoconfigure.SpringBootApplication;
import org.springframework.web.bind.annotation.*;
import org.springframework.stereotype.*;
import jakarta.persistence.*;
import org.springframework.data.jpa.repository.*;
import org.springframework.beans.factory.annotation.Autowired;
import java.util.*;

@SpringBootApplication
public class CmsApplication {
    public static void main(String[] args) {
        SpringApplication.run(CmsApplication.class, args);
    }
}

/* ---------------- ENTITY ---------------- */
@Entity
class Blog {
    @Id
    @GeneratedValue
    private Long id;

    @Lob
    private String htmlContent;

    public Blog() {}
    public Blog(String htmlContent) {
        this.htmlContent = htmlContent;
    }
    public Long getId() { return id; }
    public String getHtmlContent() { return htmlContent; }
}

/* ---------------- REPOSITORY ---------------- */
interface BlogRepo extends JpaRepository<Blog, Long> {}

/* ---------------- CONTROLLER ---------------- */
@Controller
class CmsController {

    @Autowired
    BlogRepo repo;

    @GetMapping("/")
    @ResponseBody
    public String editor() {
        return """
<!DOCTYPE html>
<html>
<head>
<title>Java CMS</title>
<style>
#toolbox div { padding:10px; border:1px solid #000; margin:5px; cursor:grab;}
#canvas { border:2px dashed gray; min-height:200px; padding:10px;}
</style>
</head>
<body>

<h2>Drag & Drop Page Builder</h2>

<div id="toolbox">
  <div draggable="true" ondragstart="drag(event)">Text</div>
  <div draggable="true" ondragstart="drag(event)">Image</div>
</div>

<div id="canvas" ondrop="drop(event)" ondragover="allowDrop(event)">
Drop Here
</div>

<h3>Blog Editor</h3>
<textarea id="editor" rows="6" cols="60"></textarea><br><br>
<button onclick="publish()">Publish</button>

<script>
function allowDrop(ev){ev.preventDefault();}
function drag(ev){ev.dataTransfer.setData("text", ev.target.innerText);}
function drop(ev){
 ev.preventDefault();
 let data = ev.dataTransfer.getData("text");
 if(data=="Text") ev.target.innerHTML += "<p contenteditable='true'>Editable Text</p>";
 if(data=="Image") ev.target.innerHTML += "<img src='https://via.placeholder.com/150'/>";
}

function publish(){
 let html = document.getElementById("canvas").innerHTML +
            "<hr>" + document.getElementById("editor").value;
 fetch('/publish',{
   method:'POST',
   headers:{'Content-Type':'text/plain'},
   body:html
 }).then(()=>alert("Published Successfully"));
}
</script>

<a href="/blogs">View Blogs</a>
</body>
</html>
""";
    }

    @PostMapping("/publish")
    @ResponseBody
    public void publish(@RequestBody String html) {
        repo.save(new Blog(html));
    }

    @GetMapping("/blogs")
    @ResponseBody
    public String viewBlogs() {
        StringBuilder sb = new StringBuilder("<h2>Published Blogs</h2>");
        for (Blog b : repo.findAll()) {
            sb.append("<div>").append(b.getHtmlContent()).append("</div><hr>");
        }
        return sb.toString();
    }
}
