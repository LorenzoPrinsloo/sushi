package io.roflsoft

import java.io.{FileWriter, StringWriter, Writer}

import sbt._
import Keys._
import de.zalando.beard.ast.BeardTemplate
import de.zalando.beard.renderer.{BeardTemplateRenderer, ClasspathTemplateLoader, CustomizableTemplateCompiler, StringWriterRenderResult, TemplateName}

object CodeGen extends AutoPlugin {

  override lazy val projectSettings = Seq(commands += crud)

  lazy val loader = new ClasspathTemplateLoader(
    templatePrefix = "/templates/",
    templateSuffix = ".beard"
  )
  lazy val templateCompiler = new CustomizableTemplateCompiler(templateLoader = loader)

  /**
   * Generates crud routes for a predefined model
   *  ex. gen-crud roflsoft.models.Person templates.crud
   *
   * @return
   */
  def crud: Command = Command.args("gen-crud", "<path>") { (state, args) =>
    val className: String = args.head
    val context: Map[String, String] = Map("className" -> className)

    val body = templateCompiler.compile(TemplateName("index")).fold(
      throwable => throwable.getMessage,
      beardTemplate => renderTemplate(beardTemplate, context).toString
    )
    writeScala(body, className + "Router")
    state
  }

  private def renderTemplate(beardTemplate: BeardTemplate, context: Map[String, String]): StringWriter = {
    println(s"Found template $beardTemplate")
    val renderer = new BeardTemplateRenderer(templateCompiler)
    renderer.render(beardTemplate,
      StringWriterRenderResult(),
      context)
  }

  private def writeScala(body: String, fileName: String): Unit = {
    val path = new File("./assets/")
    path.mkdir()
    val writer: Writer = new FileWriter(s"$path/$fileName.scala")

    writer.write(body)
    writer.close()
  }
}
