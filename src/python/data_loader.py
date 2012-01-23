#!/usr/bin/env python
import os
import optparse
import mechanize
import BeautifulSoup

DATA_URLS = (
    'ajax_sections',
    'ajax_rooms',
    'ajax_resourcetypes',
    'ajax_times',
    'ajax_teachers',
)

def main():
    parser = optparse.OptionParser()
    parser.add_option("-l", "--list-programs",
                      action="store_true", dest="list_programs")
    parser.add_option("-o", "--host", dest="host")
    parser.add_option("-u", "--username", dest="username")
    parser.add_option("-p", "--password", dest="password")
    parser.add_option("-r", "--program", dest="program")
    parser.add_option("-t", "--target-dir", dest="target_dir")

    (options, args) = parser.parse_args()

    if options.list_programs:
        list_programs(options.host, options.username, options.password)
    else:
        load_data(options.host, options.username, options.password, options.program, options.target_dir)

def login(host, username, password):
    br = mechanize.Browser()
    br.open('https://%s/' % host)
    br.select_form(name=br.forms().next().name)
    br['username'] = username
    br['password'] = password
    response = br.submit()
    return br

def list_programs(host, username, password):
    ''' Obtain a list of programs and print them to the user. '''
    browser = login(host, username, password)
    result = browser.open('https://%s/manage/programs/' % host)
    document = BeautifulSoup.BeautifulSoup(result.read())
    hrefs = ['/'.join(a['href'].split('/')[2:-1]) for a in document.findAll('a')
             if a['href'].endswith('/main')]
    print "List of available programs to pick from:"
    print "=" * 40
    print os.linesep.join(' - ' + href for href in hrefs[::-1])


def load_data(host, username, password, program_string, target_dir):
    browser = login(host, username, password)

    for data_url in DATA_URLS:
        url = 'https://%s/manage/%s/%s' % (
            host,
            program_string,
            data_url,
            )
        result = browser.open(url).read()
        file_name = os.path.join(target_dir, data_url)
        dir_name = os.path.dirname(file_name)
        try:
            os.makedirs(dir_name)
        except OSError, e:
            pass
        with open(file_name, 'w') as target_file:
            target_file.write(result)



if __name__ == '__main__':
    main()
