"""Compone el manual PDF desde la guía y el inventario de código documentado."""
from pathlib import Path
import collections
import html
import json
import re
from reportlab.lib import colors
from reportlab.lib.enums import TA_LEFT
from reportlab.lib.pagesizes import A4
from reportlab.lib.styles import ParagraphStyle, getSampleStyleSheet
from reportlab.pdfbase import pdfmetrics
from reportlab.pdfbase.ttfonts import TTFont
from reportlab.platypus import BaseDocTemplate, Frame, PageTemplate, Paragraph, Spacer, PageBreak, KeepTogether
from reportlab.platypus.tableofcontents import TableOfContents

ROOT=Path(__file__).resolve().parents[2]
OUT=ROOT/'docs/documentacion'
FONT=Path('C:/Windows/Fonts')
for name,file in [('Manual','arial.ttf'),('ManualBold','arialbd.ttf'),('ManualItalic','ariali.ttf')]:
    pdfmetrics.registerFont(TTFont(name,str(FONT/file)))
pdfmetrics.registerFontFamily('Manual',normal='Manual',bold='ManualBold',italic='ManualItalic',boldItalic='ManualBold')
INK=colors.HexColor('#16384b'); ACCENT=colors.HexColor('#147d83')
styles=getSampleStyleSheet()
styles.add(ParagraphStyle(name='BodyManual',fontName='Manual',fontSize=10,leading=14.5,textColor=INK,spaceAfter=9))
styles.add(ParagraphStyle(name='Chapter',fontName='ManualBold',fontSize=18,leading=23,textColor=INK,spaceBefore=18,spaceAfter=13,keepWithNext=True))
styles.add(ParagraphStyle(name='File',fontName='ManualBold',fontSize=13,leading=17,textColor=ACCENT,spaceBefore=18,spaceAfter=8,keepWithNext=True))
styles.add(ParagraphStyle(name='Member',fontName='Manual',fontSize=9,leading=12.5,textColor=INK,spaceAfter=7,splitLongWords=True))
styles.add(ParagraphStyle(name='PathManual',fontName='Manual',fontSize=8,leading=11,textColor=colors.HexColor('#647985'),spaceAfter=8,wordWrap='CJK'))
styles.add(ParagraphStyle(name='CoverTitle',fontName='ManualBold',fontSize=40,leading=45,textColor=INK,spaceAfter=18))
styles.add(ParagraphStyle(name='CoverSub',fontName='Manual',fontSize=21,leading=29,textColor=ACCENT,spaceAfter=25))

class Manual(BaseDocTemplate):
    def __init__(self,path):
        super().__init__(str(path),pagesize=A4,leftMargin=48,rightMargin=48,topMargin=42,bottomMargin=44,
                         title='Pequelandia - Documentación del proyecto',author='')
        self.addPageTemplates(PageTemplate(id='normal',frames=[Frame(48,44,A4[0]-96,A4[1]-86,id='body',leftPadding=0,rightPadding=0,topPadding=0,bottomPadding=0)],onPage=self.footer))
        self.index=0
    def footer(self,canvas,doc):
        if doc.page==1:return
        canvas.setFont('Manual',8);canvas.setFillColor(colors.HexColor('#647985'))
        canvas.drawString(48,25,'PEQUELANDIA  /  DOCUMENTACIÓN TÉCNICA')
        canvas.drawRightString(A4[0]-48,25,str(doc.page))
    def afterFlowable(self,flowable):
        if isinstance(flowable,Paragraph) and flowable.style.name in ('Chapter','File'):
            self.index+=1; key='section-'+str(self.index)
            self.canv.bookmarkPage(key)
            level=0 if flowable.style.name=='Chapter' else 1
            self.canv.addOutlineEntry(flowable.getPlainText(),key,level=level,closed=True)
            if level==0 and flowable.getPlainText()!='Contenido':self.notify('TOCEntry',(0,flowable.getPlainText(),self.page,key))
    def beforeDocument(self):self.index=0

def esc(s):return html.escape(str(s),quote=False)
def p(s,style='BodyManual'):return Paragraph(s,styles[style])

def main():
    inventory=json.loads((OUT/'inventario.json').read_text(encoding='utf-8'))
    coverage=json.loads((OUT/'cobertura.json').read_text(encoding='utf-8'))
    story=[Spacer(1,105),p('PEQUELANDIA','CoverTitle'),p('Documentación completa<br/>del proyecto','CoverSub'),
           p('Arquitectura, datos, recorridos, configuración y catálogo de clases y métodos.'),Spacer(1,24),
           p('Edición: 1 de noviembre de 2026'),
           p(f"{coverage['java_files']} archivos Java · {coverage['nodes']['METHOD']} métodos y constructores explícitos · {coverage['resource_files']} recursos propios documentados"),
           Spacer(1,30),p('La referencia HTML acompaña este manual con las variables locales, los parámetros y las instrucciones, el código completo numerado y un buscador.','BodyManual'),PageBreak(),p('Contenido','Chapter')]
    toc=TableOfContents();toc.levelStyles=[ParagraphStyle(name='TOCManual',fontName='Manual',fontSize=10,leading=15,spaceBefore=5,leftIndent=0,firstLineIndent=0)]
    story.extend([toc,PageBreak()])
    guide=(OUT/'MANUAL_PROYECTO.md').read_text(encoding='utf-8')
    paragraphs=re.split(r'\n\s*\n',guide)
    for para in paragraphs:
        para=para.strip()
        if para.startswith('# '):continue
        if para.startswith('## '):story.append(p(esc(para[3:]),'Chapter'))
        else:story.append(p(esc(para).replace('\n',' ')))
    story.extend([PageBreak(),p('17. Catálogo técnico de Java','Chapter'),
                  p('Cada entrada identifica el archivo, su responsabilidad, sus atributos y sus métodos explícitos. Los parámetros, variables locales e instrucciones detalladas se consultan en REFERENCIA_COMPLETA.html; el código conserva Javadoc con las entradas y salidas. Los métodos generados por Lombok se explican mediante sus anotaciones.')])
    groups=collections.defaultdict(list)
    for item in inventory:
        path=Path(item['path'])
        group='Pruebas Java' if '/test/' in item['path'] else ('Inicio' if path.parent.name=='pequelandia' else path.parent.name)
        groups[group].append(item)
    order=['Inicio','config','model','dto','repository','service','controller','util','Pruebas Java']
    first_group=True
    for group in order:
        if group not in groups:continue
        if not first_group:story.append(PageBreak())
        first_group=False
        story.append(p('Catálogo: '+group,'Chapter'))
        for item in groups[group]:
            path=Path(item['path']); nodes=item['nodes']
            intro=[p(esc(path.stem),'File'),p(esc(item['path']),'PathManual')]
            types=[n for n in nodes if n['cat']=='TYPE']
            for n in types:intro.append(p(esc(n['explanation'])))
            if not types:intro.append(p('Archivo de documentación del paquete.'))
            story.append(KeepTogether(intro))
            fields=[n for n in nodes if n['cat']=='VARIABLE' and n['owner'] in ('CLASS','RECORD','INTERFACE','ENUM')]
            if fields:story.append(p('<b>Atributos y componentes</b>','Member'))
            for n in fields:story.append(p('<b>'+esc(n['name'])+'</b> - '+esc(n['explanation']),'Member'))
            methods=[n for n in nodes if n['cat']=='METHOD']
            if methods:story.append(p('<b>Métodos y constructores</b>','Member'))
            for n in methods:
                name=path.stem if n['name']=='<init>' else n['name']
                story.append(p('<b>'+esc(name)+'</b> <font color="#647985">(línea '+str(n['line'])+')</font><br/>'+esc(n['explanation']),'Member'))
    story.extend([PageBreak(),p('18. Recursos propios y archivos de soporte','Chapter')])
    resources=[]
    for folder,pattern in [('src/main/resources/templates','*.html'),('src/main/resources/static/js','*.js'),('src/main/resources/static/css','*.css'),('src/test/js','*.cjs'),('src/main/resources/db','*.sql')]:
        resources.extend((ROOT/folder).rglob(pattern))
    resources.extend([ROOT/'pom.xml',ROOT/'compose.yaml',ROOT/'application-example.properties',ROOT/'src/main/resources/application.properties'])
    for path in sorted(resources):
        story.append(p(esc(path.relative_to(ROOT).as_posix()),'File'))
        desc={'.html':'Plantilla o fragmento Thymeleaf. La referencia explica por línea su estructura, expresiones y scripts embebidos.',
              '.js':'Script propio del navegador. La referencia explica declaraciones, eventos, peticiones HTTP y modificaciones del DOM.',
              '.cjs':'Prueba JavaScript ejecutada mediante node --test.',
              '.css':'Hoja de estilos propia: selectores, propiedades y reglas de presentación.',
              '.sql':'Esquema aditivo de correo, historial, alertas y recuperación.',
              '.properties':'Configuración de aplicación o ejemplo sin credenciales privadas.',
              '.xml':'Configuración Maven: dependencias, versión de Java y plugins de compilación.',
              '.yaml':'Definición del servicio PostgreSQL de desarrollo con Docker Compose.'}[path.suffix]
        story.append(p(desc))
    story.extend([PageBreak(),p('19. Cobertura y uso de la referencia','Chapter'),
                  p(f"El inventario contiene {coverage['nodes']['TYPE']} declaraciones de tipo, {coverage['nodes']['METHOD']} métodos o constructores, {coverage['nodes']['VARIABLE']} declaraciones de variables y parámetros y {coverage['nodes']['STATEMENT']} instrucciones Java. Estas categorías pueden estar anidadas: una condición contiene otras instrucciones y un método contiene variables."),
                  p('Abrir REFERENCIA_COMPLETA.html, escribir el archivo o método en el buscador y expandir el resultado. Cada archivo ofrece el código completo numerado y las explicaciones de sus declaraciones e instrucciones. Las expresiones que se extienden por varias líneas deben leerse como un bloque completo.'),
                  p('cobertura.json conserva los conteos y las comprobaciones de equivalencia de tokens y del árbol sintáctico Java. inventario.json permite reutilizar el catálogo en herramientas. VERIFICACION.md registra los resultados concretos de las comprobaciones de esta edición.')])
    target=OUT/'Pequelandia_Documentacion_Completa.pdf'
    Manual(target).multiBuild(story)
    print(target)

if __name__=='__main__':main()
