FROM squidfunk/mkdocs-material:5.5.14
RUN /usr/local/bin/python -m pip install --upgrade pip
RUN pip install mkdocs-macros-plugin
RUN pip install mkdocs-git-revision-date-plugin

