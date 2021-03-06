#!/usr/bin/python3

import docutils.core
import docutils.io
import docutils.nodes
import functools
import os.path
import re
import shutil
import sys

# Monkey-patch docutils.nodes.document.set_id to generate IDs from
# function names and argument counts. We could get at the doctree in
# between parsing and writing, but the bad IDs have already been
# generated by that time.
c3dfn = re.compile(r'([a-z]+3d)\(([^)]*)\)')
old_set_id = docutils.nodes.document.set_id
def new_set_id(self, node, *args, **kwargs):
  if not node['ids']:
    for name in node['names']:
      for match in c3dfn.finditer(name):
        fname = match.group(1)
        largs = match.group(2)
        nargs = largs.count(',') + 1 if largs else 0
        id = "{}-{}".format(fname, nargs)
        if id not in self.ids:
          node['ids'].append(id)
  return old_set_id(self, node, *args, **kwargs)
functools.update_wrapper(new_set_id, old_set_id)
docutils.nodes.document.set_id = new_set_id

settings_overrides={'output_encoding': 'unicode',
                    'embed_stylesheet': False,
                    }
parts = docutils.core.publish_parts(
    source_class=docutils.io.FileInput,
    source=None,
    source_path='CommandReference.rst',
    reader=None, reader_name='standalone',
    parser=None, parser_name='restructuredtext',
    writer=None, writer_name='html',
    settings_overrides=settings_overrides,
    )
body = parts['html_body']
title = parts['title']
m = re.search('href=(["\'])([^"\']*)\\1',
              parts['stylesheet'], re.IGNORECASE)
css = m.group(2)
cssn = os.path.join('..', 'stylesheets', os.path.basename(css))
os.chdir(sys.argv[1])
shutil.copyfile(css, cssn)
with open('CommandReference.html', 'w', encoding='utf-8') as f:
    f.write('''\
---
layout: main
title: Cindy 3D {title}
styles: [{cssn}, ../stylesheets/docutils-overrides.css]
topdir: ../
---
{body}'''.format_map(locals()))
